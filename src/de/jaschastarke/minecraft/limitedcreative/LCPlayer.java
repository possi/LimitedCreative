/*
 * Limited Creative - (Bukkit Plugin)
 * Copyright (C) 2012 jascha@ja-s.de
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.jaschastarke.minecraft.limitedcreative;

import static de.jaschastarke.minecraft.utils.Locale.L;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import de.jaschastarke.minecraft.limitedcreative.serialize.Items;

public class LCPlayer {
    private static LimitedCreativeCore plugin = LimitedCreativeCore.plugin;
    private static Map<Player, LCPlayer> players = new HashMap<Player, LCPlayer>();
    private Player player;
    private Map<Integer, ItemStack> tempinv = null;
    
    private static File _store_file = new File(plugin.getDataFolder(), "players.yml");
    public static YamlConfiguration store = YamlConfiguration.loadConfiguration(_store_file);
    
    private boolean isPermanentCreative = false;
    private boolean _isRegionCreative = false;

    private LCPlayer(Player pplayer) {
        player = pplayer;
        _isRegionCreative = store.getBoolean(player.getName()+".region_creative", false);
        if (player.getGameMode() == GameMode.CREATIVE && !this.getRegionCreative())
            isPermanentCreative = true;
    }
    
    public Player getRaw() {
        return player;
    }
    
    public static LCPlayer get(Player pplayer) {
        if (!players.containsKey(pplayer)) {
            LCPlayer p = new LCPlayer(pplayer);
            players.put(pplayer, p);
            return p;
        } else {
            return players.get(pplayer);
        }
    }
    
    public boolean getRegionCreative() {
        return _isRegionCreative;
    }
    public void setRegionCreative(boolean b) {
        if (b) {
            store.set(player.getName()+".region_creative", true);
        } else {
            store.set(player.getName(), null);
        }
        try {
            store.save(_store_file);
        } catch (IOException e) {
            plugin.logger.severe("Failed to save players.yml");
            e.printStackTrace();
        }
        _isRegionCreative = b;
    }
    
    public void onSetCreative() {
        if (!this.getRegionCreative())
            isPermanentCreative = true;
        if (plugin.config.getStoreEnabled()) {
            if (plugin.config.getPermissionToKeepInventory() && player.hasPermission("limitedcreative.keepinventory"))
                return;
            Inventory inv = new Inventory(player);
            inv.save();
            if (plugin.config.getStoreCreative() && inv.isStored(GameMode.CREATIVE)) {
                inv.load(GameMode.CREATIVE);
            } else {
                inv.clear();
            }
        }
    }
    public void onSetSurvival() {
        isPermanentCreative = false;
        setRegionCreative(false);
        if (plugin.config.getStoreEnabled()) {
            if (plugin.config.getPermissionToKeepInventory() && player.hasPermission("limitedcreative.keepinventory"))
                return;
            Inventory inv = new Inventory(player);
            if (plugin.config.getStoreCreative()) {
                inv.save();
            }
            if (inv.isStored(GameMode.SURVIVAL))
                inv.load(GameMode.SURVIVAL);
        }
    }
    public void onDropItem(PlayerDropItemEvent event) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            if (plugin.config.getPermissionsEnabled() && event.getPlayer().hasPermission("limitedcreative.nolimit.drop"))
                return;
            event.setCancelled(true);
        }
    }
    public void onPickupItem(PlayerPickupItemEvent event) {
        if (player.getGameMode() == GameMode.CREATIVE && plugin.config.getBlockPickupInCreative()) {
            if (plugin.config.getPermissionsEnabled() && event.getPlayer().hasPermission("limitedcreative.nolimit.pickup"))
                return;
            event.setCancelled(true);
        }
    }
    
    public void onDie(EntityDeathEvent event) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            if (!plugin.config.getPermissionsEnabled() || !player.hasPermission("limitedcreative.nolimit.drop")) {
                event.getDrops().clear();
                tempinv = Items.storeInventory(player.getInventory());
            }
        }
    }
    public void onRespawn(PlayerRespawnEvent event) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            if (!plugin.config.getPermissionsEnabled() || !player.hasPermission("limitedcreative.nolimit.drop")) {
                if (tempinv != null) {
                    Items.restoreInventory(player.getInventory(), tempinv);
                }
            }
        }
        tempinv = null;
    }
    
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            // its PVP
            Player attacker = (Player) event.getDamager();
            if (attacker.getGameMode() == GameMode.CREATIVE) {
                if (!plugin.config.getPermissionsEnabled() || !attacker.hasPermission("limitedcreative.nolimit.pvp")) {
                    event.setCancelled(true);
                }
            }
            if (player.getGameMode() == GameMode.CREATIVE) {
                if (!plugin.config.getPermissionsEnabled() || !player.hasPermission("limitedcreative.nolimit.pvp")) {
                    event.setCancelled(true);
                }
            }
        }
    }
    public void onChestAccess(PlayerInteractEvent event) {
        if (player.getGameMode() != GameMode.CREATIVE)
            return;
        if (plugin.config.getPermissionsEnabled() && event.getPlayer().hasPermission("limitedcreative.nolimit.chest"))
            return;
        event.getPlayer().sendMessage(L("blocked.chest"));
        event.setCancelled(true);
    }
    public void onChestAccess(PlayerInteractEntityEvent event) { // chest-minecarts are different events
        if (player.getGameMode() != GameMode.CREATIVE)
            return;
        if (plugin.config.getPermissionsEnabled() && event.getPlayer().hasPermission("limitedcreative.nolimit.chest"))
            return;
        event.getPlayer().sendMessage(L("blocked.chest"));
        event.setCancelled(true);
    }
    public void onSignAccess(PlayerInteractEvent event) {
        if (!plugin.config.getSignBlock() || player.getGameMode() != GameMode.CREATIVE)
            return;
        if (plugin.config.getPermissionsEnabled() && event.getPlayer().hasPermission("limitedcreative.nolimit.sign"))
            return;
        event.getPlayer().sendMessage(L("blocked.sign"));
        event.setCancelled(true);
    }

    public void setRegionCreativeAllowed(boolean rcreative, PlayerMoveEvent event) {
        if (rcreative && player.getGameMode() == GameMode.SURVIVAL) {
            setRegionCreative(true); // have to be set, before setGameMode
            player.setGameMode(GameMode.CREATIVE);
        } else if (!rcreative && player.getGameMode() == GameMode.CREATIVE && !isPermanentCreative) {
            if (getFloatingHeight() > 3) {
                player.sendMessage(L("blocked.survival_flying"));
                event.setTo(event.getFrom());
            } else {
                player.setGameMode(GameMode.SURVIVAL); // also unsets isRegionCreative;
            }
        }
    }
    
    public int getFloatingHeight() {
        Block b = player.getLocation().getBlock();
        int steps = 0;
        while (b.getType() == Material.AIR) {
            steps++;
            b = b.getRelative(BlockFace.DOWN);
        }
        return steps;
    }
    
    public void goToFloor() {
        Block b = player.getLocation().getBlock();
        int steps = 0;
        while (b.getType() == Material.AIR) {
            steps++;
            b = b.getRelative(BlockFace.DOWN);
        }
        if (steps > 2) {
            player.teleport(new Location(player.getWorld(),
                    player.getLocation().getX(),
                    b.getY()+1,
                    player.getLocation().getZ()));
        }
    }
}
