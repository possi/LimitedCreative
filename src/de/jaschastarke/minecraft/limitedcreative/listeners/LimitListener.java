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
package de.jaschastarke.minecraft.limitedcreative.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.ContainerBlock;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;

import de.jaschastarke.minecraft.limitedcreative.BlackList;
import de.jaschastarke.minecraft.limitedcreative.LCPlayer;
import de.jaschastarke.minecraft.limitedcreative.LimitedCreativeCore;
import static de.jaschastarke.minecraft.utils.Locale.L;

public class LimitListener implements Listener {
    private LimitedCreativeCore plugin;
    public LimitListener(LimitedCreativeCore plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        LCPlayer.get(event.getPlayer()).onRespawn(event);
    }


    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        LCPlayer.get(event.getPlayer()).onDropItem(event);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        LCPlayer.get(event.getPlayer()).onPickupItem(event);
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (MainListener.isCancelled(event) || event.getPlayer().getGameMode() != GameMode.CREATIVE)
            return;
        
        LCPlayer player = LCPlayer.get(event.getPlayer());
        if (!plugin.config.getPermissionsEnabled() || !player.hasPermission("limitedcreative.nolimit.use")) {
            if (event.getItem() != null && BlackList.isBlackListed(plugin.config.getBlockedUse(), event.getItem())) {
                event.setCancelled(true);
                event.setUseItemInHand(Event.Result.DENY);
                event.getPlayer().sendMessage(L("blocked.use"));
                return;
            }
        }
        
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        
        Block block = event.getClickedBlock();
        
        if (block.getState() instanceof ContainerBlock) {
            player.onChestAccess(event);
        } else if (block.getState() instanceof Sign) {
            player.onSignAccess(event);
        } else if (block.getState() instanceof Lever || block.getState() instanceof Button) {
            player.onButtonAccess(event);
        }
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.isCancelled() || event.getPlayer().getGameMode() != GameMode.CREATIVE)
            return;

        LCPlayer player = LCPlayer.get(event.getPlayer());
        if (!plugin.config.getPermissionsEnabled() || !player.hasPermission("limitedcreative.nolimit.use")) {
            if (event.getPlayer().getItemInHand() != null && BlackList.isBlackListed(plugin.config.getBlockedUse(), event.getPlayer().getItemInHand())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(L("blocked.use"));
                return;
            }
        }
        
        Entity entity = event.getRightClicked();

        if (entity instanceof StorageMinecart) {
            player.onChestAccess(event);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent meta_event) {
        if (meta_event.isCancelled())
            return;
        if (meta_event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) meta_event;
            if (event.getEntity() instanceof Player) {
                LCPlayer.get((Player) event.getEntity()).onDamage(event);
            }
            if (!event.isCancelled() && event.getDamager() instanceof Player){
                LCPlayer.get((Player) event.getDamager()).onDealDamage(event);
            }
        }
    }
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.isCancelled())
            return;
        if (event.getTarget() instanceof Player) {
            LCPlayer.get((Player) event.getTarget()).onTarget(event);
        }
    }

    @EventHandler(priority=EventPriority.LOW)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            LCPlayer.get(player).onDie(event);
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            LCPlayer player = LCPlayer.get(event.getPlayer());
            if (!plugin.config.getPermissionsEnabled() || !player.hasPermission("limitedcreative.nolimit.break")) {
                if (BlackList.isBlackListed(plugin.config.getBlockedBreaks(), event.getBlock())) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(L("blocked.break"));
                }
            }
            
            if (plugin.config.getPermissionsEnabled() && player.hasPermission("limitedcreative.nolimit.drop"))
                return;
            // Prevent dropping of doors and beds when destroying the wrong part
            Block block = event.getBlock();
            Material mat = block.getType();
            switch (event.getBlock().getType()) {
                case WOODEN_DOOR:
                    mat = Material.WOOD_DOOR;
                    plugin.spawnblock.block(block.getRelative(BlockFace.DOWN).getLocation(), mat);
                    break;
                case IRON_DOOR_BLOCK:
                    mat = Material.IRON_DOOR;
                    plugin.spawnblock.block(block.getRelative(BlockFace.DOWN).getLocation(), mat);
                    break;
                case BED_BLOCK:
                    mat = Material.BED;
                    plugin.spawnblock.block(block.getRelative(BlockFace.NORTH).getLocation(), mat);
                    plugin.spawnblock.block(block.getRelative(BlockFace.EAST).getLocation(), mat);
                    plugin.spawnblock.block(block.getRelative(BlockFace.SOUTH).getLocation(), mat);
                    plugin.spawnblock.block(block.getRelative(BlockFace.WEST).getLocation(), mat);
                    break;
                default:
                    plugin.spawnblock.block(event.getBlock().getLocation(), mat);
            }
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            LCPlayer player = LCPlayer.get(event.getPlayer());
            if (!plugin.config.getPermissionsEnabled() || !player.hasPermission("limitedcreative.nolimit.use")) {
                if (BlackList.isBlackListed(plugin.config.getBlockedUse(), event.getBlock())) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(L("blocked.place"));
                }
            }
        }
    }
}