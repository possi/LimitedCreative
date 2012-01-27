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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
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

import de.jaschastarke.minecraft.limitedcreative.Commands.LackingPermissionException;
import de.jaschastarke.minecraft.limitedcreative.serialize.Items;
import static de.jaschastarke.minecraft.utils.Locale.L;

public class LCPlayer {
    private static LimitedCreativeCore plugin = LimitedCreativeCore.plugin;
    private static Map<Player, LCPlayer> players = new HashMap<Player, LCPlayer>();
    private Player player;
    private Map<Integer, ItemStack> tempinv = null;
    
    private static File _store_file = new File(plugin.getDataFolder(), "players.yml");
    public static YamlConfiguration store = YamlConfiguration.loadConfiguration(_store_file);
    
    private boolean _isPermanentCreative = false;
    private boolean _isRegionCreative = false;

    private LCPlayer(Player pplayer) {
        player = pplayer;
        _isRegionCreative = store.getBoolean(player.getName()+".region_creative", false) && player.getGameMode() == GameMode.CREATIVE;
        if (player.getGameMode() == GameMode.CREATIVE && !this.isRegionCreative())
            setPermanentCreative(true);
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
    
    public boolean isRegionCreative() {
        return _isRegionCreative;
    }

    public void changeGameMode(GameMode gm) throws LackingPermissionException {
        if (!isRegionCreative() && !getRaw().hasPermission("limitedcreative.switch_gamemode")) {
            if (gm != GameMode.SURVIVAL || !getRaw().hasPermission("limitedcreative.switch_gamemode.backonly")) {
                throw new Commands.LackingPermissionException();
            }
        }
        getRaw().setGameMode(gm);
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
    public boolean isPermanentCreative() {
        return _isPermanentCreative;
    }
    public void setPermanentCreative(boolean b) {
        _isPermanentCreative = b;
        if (b)
            setRegionCreative(false);
    }
    
    public boolean onSetCreative() {
        if (!this.isRegionCreative())
            setPermanentCreative(true);
        if (plugin.config.getStoreEnabled()) {
            if (plugin.config.getPermissionToKeepInventory() && player.hasPermission("limitedcreative.keepinventory"))
                return true;
            Inventory inv = new Inventory(player);
            inv.save();
            if (plugin.config.getStoreCreative() && inv.isStored(GameMode.CREATIVE)) {
                inv.load(GameMode.CREATIVE);
            } else {
                inv.clear();
            }
        }
        return true;
    }
    public boolean onSetSurvival() {
        if (isRegionCreative()) {
            if (!plugin.config.getRegionOptional()) {
                getRaw().sendMessage(ChatColor.RED + L("exception.region.no_survival"));
                return false;
            }
        }
        setPermanentCreative(false);
        if (plugin.config.getStoreEnabled()) {
            if (plugin.config.getPermissionToKeepInventory() && player.hasPermission("limitedcreative.keepinventory"))
                return true;
            Inventory inv = new Inventory(player);
            if (plugin.config.getStoreCreative()) {
                inv.save();
            }
            if (inv.isStored(GameMode.SURVIVAL))
                inv.load(GameMode.SURVIVAL);
        }
        return true;
    }
    public void onDropItem(PlayerDropItemEvent event) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            if (plugin.config.getPermissionsEnabled() && event.getPlayer().hasPermission("limitedcreative.nolimit.drop"))
                return;
            event.getItemDrop().remove();
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
    
    private long lastFloatingTimeWarning = 0;

    public void setRegionCreativeAllowed(boolean rcreative, PlayerMoveEvent event) {
        if (rcreative && player.getGameMode() == GameMode.SURVIVAL && !isRegionCreative()) {
            setRegionCreative(true); // have to be set, before setGameMode
            player.setGameMode(GameMode.CREATIVE);
        } else if (!rcreative && player.getGameMode() == GameMode.CREATIVE && !isPermanentCreative()) {
            if (getFloatingHeight() > 3) {
                if (System.currentTimeMillis() - lastFloatingTimeWarning > 10000) {// 10 sec. limit
                    player.sendMessage(L("blocked.survival_flying"));
                    lastFloatingTimeWarning = System.currentTimeMillis();
                }
                
                Location newloc = event.getTo().clone();
                newloc.setX(event.getFrom().getX());
                newloc.setY(event.getFrom().getY()); // well, otherwise flying high out of the region is possible
                newloc.setZ(event.getFrom().getZ());
                event.setTo(newloc);
            } else {
                setRegionCreative(false);
                player.setGameMode(GameMode.SURVIVAL); // also unsets isRegionCreative;
            }
        } else if (!rcreative && isRegionCreative()) {
            setRegionCreative(false); // we have left, while in optional survival
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
