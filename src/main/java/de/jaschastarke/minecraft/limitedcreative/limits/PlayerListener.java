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
package de.jaschastarke.minecraft.limitedcreative.limits;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.DirectionalContainer;
import org.bukkit.potion.PotionEffect;

import de.jaschastarke.minecraft.lib.permissions.IAbstractPermission;
import de.jaschastarke.minecraft.lib.permissions.IDynamicPermission;
import de.jaschastarke.minecraft.limitedcreative.ModCreativeLimits;
import de.jaschastarke.minecraft.limitedcreative.limits.LimitConfig.BlockPickup;

public class PlayerListener implements Listener {
    private ModCreativeLimits mod;
    public PlayerListener(ModCreativeLimits mod) {
        this.mod = mod;
    }
    
    /**
     * The isCancelled in PlayerInteractEvent doesn't check useItemInHand, even this decides (when clicking on
     * entity with e.g. a bucket)
     * @param event
     * @return The relevant "isCancelled"
     */
    public static boolean isCancelled(PlayerInteractEvent event) {
        return event.useInteractedBlock() == Event.Result.DENY && event.useItemInHand() == Event.Result.DENY;
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!event.isCancelled() && event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            if (checkPermission(event, NoLimitPermissions.DROP))
                return;
            
            event.getItemDrop().remove();
            //event.setCancelled(true); // doesn't make much sense
        }
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (player.getGameMode() == GameMode.CREATIVE) {
                if (checkPermission(player, NoLimitPermissions.DROP))
                    return;
                event.getDrops().clear();
            }
        }
    }
    
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (!event.isCancelled() && event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            LimitConfig.BlockPickup pickup = mod.getConfig().getBlockPickup();
            if (pickup != null) {
                if (checkPermission(event, NoLimitPermissions.DROP))
                    return;
                
                if (pickup == BlockPickup.REMOVE) {
                    event.getItem().remove();
                }
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerChangeExp(PlayerExpChangeEvent event) {
        if (event.getAmount() > 0 && event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            if (checkPermission(event, NoLimitPermissions.STATS_XP))
                return;
            event.setAmount(0);
        }
    }
    @EventHandler
    public void onEntityFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            if (!event.isCancelled() && ((Player) event.getEntity()).getGameMode() == GameMode.CREATIVE) {
                if (checkPermission((Player) event.getEntity(), NoLimitPermissions.STATS_HEALTH))
                    return;
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            if (!event.isCancelled() && ((Player) event.getEntity()).getGameMode() == GameMode.CREATIVE) {
                if (checkPermission((Player) event.getEntity(), NoLimitPermissions.STATS_HEALTH))
                    return;
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority=EventPriority.LOW)
    public void onGameMode(PlayerGameModeChangeEvent event) {
        if (!event.isCancelled()) {
            if ((event.getPlayer().getGameMode() == GameMode.CREATIVE) && (event.getNewGameMode() != GameMode.CREATIVE)) {
                if (checkPermission(event, NoLimitPermissions.STATS_POTION))
                    return;
                for (PotionEffect effect : event.getPlayer().getActivePotionEffects()) {
                    event.getPlayer().removePotionEffect(effect.getType());
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isCancelled(event) && event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            if (event.getItem() != null && mod.getConfig().getBlockUse().isListed(event.getItem())) {
                if (!checkPermission(event, NoLimitPermissions.USE(event.getItem().getData()))) {
                    event.setCancelled(true);
                    event.setUseItemInHand(Event.Result.DENY);
                    event.getPlayer().sendMessage(mod.getPlugin().getLocale().trans("blocked.use"));
                    return;
                }
            }
            
            // Well, the action could be ignored, if the player is sneaking, as MC now let you place block on workbench
            // and other while crouching.
            // But we don't trust other plugins, like chest-shops that do something while right-clicking a block even
            // when crouching.
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Block block = event.getClickedBlock();
                if (isChest(block)) {
                    if (!checkPermission(event, NoLimitPermissions.CHEST)) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(mod.getPlugin().getLocale().trans("blocked.chest"));
                        return;
                    }
                } else if (mod.getConfig().getBlockInteraction().isListed(block)) {
                    if (!checkPermission(event, NoLimitPermissions.INTERACT(block))) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(mod.getPlugin().getLocale().trans("blocked.interact"));
                        return;
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!event.isCancelled() && event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            if (mod.getConfig().getBlockUse().isListed(event.getPlayer().getItemInHand())) {
                if (!checkPermission(event, NoLimitPermissions.USE(event.getPlayer().getItemInHand().getData()))) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(mod.getPlugin().getLocale().trans("blocked.use"));
                    return;
                }
            }
            Entity entity = event.getRightClicked();
            if (mod.getConfig().getBlockEntityInteraction().isListed(entity)) {
                if (!checkPermission(event, NoLimitPermissions.BASE_INTERACT)) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(mod.getPlugin().getLocale().trans("blocked.entity"));
                    return;
                }
            }
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageEvent rawevent) {
        if (rawevent instanceof EntityDamageByEntityEvent && !rawevent.isCancelled()) {
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) rawevent;
            
            Entity source = event.getDamager();
            if (source instanceof Projectile)
                source = ((Projectile) source).getShooter();
            
            if (source instanceof Player) {
                Player player = (Player) source;
                if (player.getGameMode() == GameMode.CREATIVE) {
                    if (event.getEntity() instanceof Player) {
                        if (!checkPermission(player, NoLimitPermissions.PVP)) {
                            event.setCancelled(true);
                        }
                    } else if (event.getEntity() instanceof LivingEntity && mod.getConfig().getBlockDamageMob()) {
                        if (!checkPermission(player, NoLimitPermissions.MOB_DAMAGE)) {
                            event.setCancelled(true);
                        }
                    } else if (event.getEntity() instanceof LivingEntity) {
                        if (!checkPermission(player, NoLimitPermissions.STATS_XP)) {
                            mod.getNoXPMobs().put(event.getEntity(), null);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Returns if the block is a chest or an other inventory-holder, that can hold items.
     */
    private boolean isChest(Block block) {
        return block.getState() instanceof InventoryHolder ||
               block.getState().getData() instanceof DirectionalContainer ||
               block.getType().equals(Material.ENDER_CHEST) ||
               block.getType().equals(Material.BEACON);
    }

    private boolean checkPermission(Player player, IAbstractPermission perm) {
        return mod.getPlugin().getPermManager().hasPermission(player, perm);
    }
    private boolean checkPermission(PlayerEvent event, IAbstractPermission perm) {
        return mod.getPlugin().getPermManager().hasPermission(event.getPlayer(), perm);
    }
    private boolean checkPermission(PlayerEvent event, IDynamicPermission perm) {
        return mod.getPlugin().getPermManager().hasPermission(event.getPlayer(), perm);
    }
}
