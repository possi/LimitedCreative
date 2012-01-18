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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.PluginManager;

public final class Listener {
    private static LimitedCreativeCore plugin;
    private static PluginManager pm;
    
    public static class PlayerListen extends PlayerListener {
        @Override
        public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
            if (event.getNewGameMode() == GameMode.CREATIVE) {
                LCPlayer.get(event.getPlayer()).onSetCreative();
            } else if (event.getNewGameMode() == GameMode.SURVIVAL) {
                LCPlayer.get(event.getPlayer()).onSetSurvival();
            }
        }
        

        @Override
        public void onPlayerRespawn(PlayerRespawnEvent event) {
            LCPlayer.get(event.getPlayer()).onRespawn(event);
        }


        @Override
        public void onPlayerDropItem(PlayerDropItemEvent event) {
            LCPlayer.get(event.getPlayer()).onDropItem(event);
        }

        @Override
        public void onPlayerPickupItem(PlayerPickupItemEvent event) {
            LCPlayer.get(event.getPlayer()).onPickupItem(event);
        }

        @Override
        public void onPlayerInteract(PlayerInteractEvent event) {
            if (event.isCancelled() || event.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;
            
            Block block = event.getClickedBlock();
            
            if (block.getState() instanceof ContainerBlock) {
                LCPlayer.get(event.getPlayer()).onChestAccess(event);
            }
            if (block.getState() instanceof Sign) {
                LCPlayer.get(event.getPlayer()).onSignAccess(event);
            }
        }

        @Override
        public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
            if (event.isCancelled() || event.getPlayer().getGameMode() == GameMode.SURVIVAL)
                return;
            
            Entity entity = event.getRightClicked();

            if (entity instanceof StorageMinecart) {
                LCPlayer.get(event.getPlayer()).onChestAccess(event);
            }
        }
        
        private void register() {
            pm.registerEvent(Event.Type.PLAYER_GAME_MODE_CHANGE, this, Priority.Normal, plugin);
            pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, this, Priority.Normal, plugin);
            pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, this, Priority.Normal, plugin);
            pm.registerEvent(Event.Type.PLAYER_INTERACT, this, Priority.Lowest, plugin);
            pm.registerEvent(Event.Type.PLAYER_INTERACT_ENTITY, this, Priority.Lowest, plugin);
            pm.registerEvent(Event.Type.PLAYER_RESPAWN, this, Priority.Normal, plugin);
        }
    }
    
    public static class EntityListen extends EntityListener {
        @Override
        public void onEntityDamage(EntityDamageEvent meta_event) {
            if (meta_event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) meta_event;
                if (event.getEntity() instanceof Player) {
                    LCPlayer.get((Player) event.getEntity()).onDamage(event);
                }
            }
        }

        @Override
        public void onEntityDeath(EntityDeathEvent event) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                LCPlayer.get(player).onDie(event);
            }
        }

        private void register() {
            pm.registerEvent(Event.Type.ENTITY_DAMAGE, this, Priority.Normal, plugin);
            pm.registerEvent(Event.Type.ENTITY_DEATH, this, Priority.Low, plugin);
        }
    }
    
    public static void register(LimitedCreativeCore pplugin) {
        plugin = pplugin;
        pm = plugin.getServer().getPluginManager();
        
        new PlayerListen().register();
        new EntityListen().register();
    }
}
