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

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.plugin.PluginManager;
import static de.jaschastarke.minecraft.utils.Locale.L;

public final class Listener {
    private static LimitedCreativeCore plugin;
    private static PluginManager pm;
    
    public static class PlayerListen extends PlayerListener {
        @Override
        public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
            if (event.getNewGameMode() == GameMode.CREATIVE) {
                new PlayerCore(plugin, event.getPlayer()).onSetCreative();
            } else if (event.getNewGameMode() == GameMode.SURVIVAL) {
                new PlayerCore(plugin, event.getPlayer()).onSetSurvival();
            }
        }
        

        @Override
        public void onPlayerDropItem(PlayerDropItemEvent event) {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                if (plugin.config.getPermissionsEnabled() && event.getPlayer().hasPermission("limitedcreative.nolimit.drop"))
                    return;
                event.setCancelled(true);
            }
        }

        @Override
        public void onPlayerPickupItem(PlayerPickupItemEvent event) {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE && plugin.config.getBlockPickupInCreative()) {
                if (plugin.config.getPermissionsEnabled() && event.getPlayer().hasPermission("limitedcreative.nolimit.pickup"))
                    return;
                event.setCancelled(true);
            }
        } 

        @Override
        public void onPlayerInteract(PlayerInteractEvent event) {
            if (event.isCancelled() || event.getPlayer().getGameMode() == GameMode.SURVIVAL)
                return;

            if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;
            
            Block block = event.getClickedBlock();
            
            if (block.getState() instanceof ContainerBlock) {
                if (plugin.config.getPermissionsEnabled() && event.getPlayer().hasPermission("limitedcreative.nolimit.chest"))
                    return;
                event.getPlayer().sendMessage(L("blocked.chest"));
                event.setCancelled(true);
            }
            if (plugin.config.getSignBlock() && block.getState() instanceof Sign) {
                if (plugin.config.getPermissionsEnabled() && event.getPlayer().hasPermission("limitedcreative.nolimit.sign"))
                    return;
                event.getPlayer().sendMessage(L("blocked.sign"));
                event.setCancelled(true);
            }
        }

        @Override
        public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
            if (event.isCancelled() || event.getPlayer().getGameMode() == GameMode.SURVIVAL)
                return;
            
            Entity entity = event.getRightClicked();

            if (entity instanceof StorageMinecart) {
                if (plugin.config.getPermissionsEnabled() && event.getPlayer().hasPermission("limitedcreative.nolimit.chest"))
                    return;
                event.getPlayer().sendMessage(L("blocked.chest"));
                event.setCancelled(true);
            }
        }
        
        private void register() {
            pm.registerEvent(Event.Type.PLAYER_GAME_MODE_CHANGE, this, Priority.Normal, plugin);
            pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, this, Priority.Normal, plugin);
            pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, this, Priority.Normal, plugin);
            pm.registerEvent(Event.Type.PLAYER_INTERACT, this, Priority.Lowest, plugin);
            pm.registerEvent(Event.Type.PLAYER_INTERACT_ENTITY, this, Priority.Lowest, plugin);
        }
    }
    
    public static class EntityListen extends EntityListener {
        @Override
        public void onEntityDamage(EntityDamageEvent meta_event) {
            if (meta_event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) meta_event;
                if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
                    // its PVP
                    Player attacker = (Player) event.getDamager();
                    Player attacked = (Player) event.getEntity();
                    if (attacker.getGameMode() == GameMode.CREATIVE) {
                        if (!plugin.config.getPermissionsEnabled() || !attacker.hasPermission("limitedcreative.nolimit.pvp")) {
                            event.setCancelled(true);
                        }
                    }
                    if (attacked.getGameMode() == GameMode.CREATIVE) {
                        if (!plugin.config.getPermissionsEnabled() || !attacked.hasPermission("limitedcreative.nolimit.pvp")) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }

        private void register() {
            pm.registerEvent(Event.Type.ENTITY_DAMAGE, this, Priority.Normal, plugin);
        }
    }
    
    public static void register(LimitedCreativeCore pplugin) {
        plugin = pplugin;
        pm = plugin.getServer().getPluginManager();
        
        new PlayerListen().register();
        new EntityListen().register();
    }
}
