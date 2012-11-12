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

import static de.jaschastarke.minecraft.utils.Locale.L;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.event.Cancellable;
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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;

import de.jaschastarke.minecraft.limitedcreative.BlackList;
import de.jaschastarke.minecraft.limitedcreative.Core;
import de.jaschastarke.minecraft.limitedcreative.LCPlayer;
import de.jaschastarke.minecraft.limitedcreative.Perms;
import de.jaschastarke.minecraft.limitedcreative.Players;

public class LimitListener implements Listener {
    private Core plugin;
    public LimitListener(Core plugin) {
        this.plugin = plugin;
    }
    

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Players.get(event.getPlayer()).onDropItem(event);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Players.get(event.getPlayer()).onPickupItem(event);
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (MainListener.isCancelled(event) || event.getPlayer().getGameMode() != GameMode.CREATIVE)
            return;
        
        LCPlayer player = Players.get(event.getPlayer());
        if (!player.hasPermission(Perms.NoLimit.USE)) {
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
        
        if (block.getState() instanceof InventoryHolder || block.getType() == Material.ENDER_CHEST) { // Workaround, Bukkit not recognize a Enderchest
            player.onChestAccess(event);
        } else if (block.getState() instanceof Sign) {
            player.onSignAccess(event);
        } else if (block.getState() instanceof Lever || block.getState() instanceof Button) {
            player.onButtonAccess(event);
        } else if (block.getType() == Material.WORKBENCH) {
            player.onBenchAccess(event);
        }
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.isCancelled() || event.getPlayer().getGameMode() != GameMode.CREATIVE)
            return;

        LCPlayer player = Players.get(event.getPlayer());
        if (!player.hasPermission(Perms.NoLimit.USE)) {
            if (event.getPlayer().getItemInHand() != null && BlackList.isBlackListed(plugin.config.getBlockedUse(), event.getPlayer().getItemInHand())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(L("blocked.use"));
                return;
            }
        }
        Entity entity = event.getRightClicked();

        // Temporary Solution: While dropping of Items is prevented we don't allow Interaction with ItemFrames, so no
        // Items can be "placed" anywhere.
        if (!player.hasPermission(Perms.NoLimit.DROP)) {
            if (entity instanceof ItemFrame && plugin.config.getRemoveDrop()) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(L("blocked.use"));
                return;
            }
        }

        if (entity instanceof StorageMinecart) {
            player.onChestAccess(event);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent)
            onEntityDamageByEntity((EntityDamageByEntityEvent) event);
    }
    
    /*
     * Registering to that event works, but causes a SEVERE:
     *   Plugin attempted to register delegated event class class org.bukkit.event.entity.EntityDamageByEntityEvent.
     *   It should be using class org.bukkit.event.entity.EntityDamageEvent!
     */
    protected void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled())
            return;
        Entity source = event.getDamager();
        if (source instanceof Projectile)
            source = ((Projectile) source).getShooter();
        
        if (event.getEntity() instanceof Player) {
            Players.get((Player) event.getEntity()).onDamage(source, event);
        }
        if (!event.isCancelled() && source instanceof Player) {
            Players.get((Player) source).onDealDamage(event);
        }
    }
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.isCancelled())
            return;
        if (event.getTarget() instanceof Player) {
            Players.get((Player) event.getTarget()).onTarget(event);
        }
    }

    @EventHandler(priority=EventPriority.LOW)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Players.get(player).onDie(event);
        }
    }
    /*@EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Players.get(event.getPlayer()).onRespawn(event);
    }*/
    
    
    private void whenBlockBreak(Cancellable event, Block block, Player eventPlayer) {
        if (eventPlayer.getGameMode() == GameMode.CREATIVE) {
            LCPlayer player = Players.get(eventPlayer);
            if (!player.hasPermission(Perms.NoLimit.BREAK)) {
                if (BlackList.isBlackListed(plugin.config.getBlockedBreaks(), block)) {
                    event.setCancelled(true);
                    eventPlayer.sendMessage(L("blocked.break"));
                }
            }
            
            if (player.hasPermission(Perms.NoLimit.DROP))
                return;
            // Prevent dropping of doors and beds when destroying the wrong part
            
            // TODO: Fix, Remove, or make it god like, but this little thing is crap ;)
            Material mat = block.getType();
            switch (block.getType()) {
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
                    plugin.spawnblock.block(block.getLocation(), mat);
            }
        }
    }
    
    /* Doesn't make sense yet, as the block always will be Air. whenBlockBreak should be changed to use Material
     * instead. Maybe in the Feature.
    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player) {
            Player eventPlayer = (Player) event.getRemover();
            whenBlockBreak(event, event.getEntity().getLocation().getBlock(), eventPlayer);
        }
    }*/
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        whenBlockBreak(event, event.getBlock(), event.getPlayer());
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            LCPlayer player = Players.get(event.getPlayer());
            if (!player.hasPermission(Perms.NoLimit.USE)) {
                if (BlackList.isBlackListed(plugin.config.getBlockedUse(), event.getBlock())) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(L("blocked.place"));
                }
            }
        }
    }
}