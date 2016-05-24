package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.util.Date;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import de.jaschastarke.minecraft.limitedcreative.ModBlockStates;

public class HangingStandingListener implements Listener {
    private ModBlockStates mod;
    public HangingStandingListener(ModBlockStates mod) {
        this.mod = mod;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (mod.getConfig().getIgnoredWorlds().contains(event.getRightClicked().getWorld().getName()))
            return;
        if (event.getRightClicked() instanceof ItemFrame) {
            if (mod.getModel().isRestricted(event.getRightClicked().getLocation().getBlock())) {
                if (mod.isDebug())
                    mod.getLog().debug("Modifying hanging: " + event.getRightClicked().getLocation().toString());
                
                if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    if (mod.isDebug())
                        mod.getLog().debug("... was placed by creative. Modify prevented");
                    event.setCancelled(true);
                }
            } else {
                BlockState s = new BlockState();
                s.setLocation(event.getRightClicked().getLocation().getBlock().getLocation());
                s.setPlayer(event.getPlayer());
                s.setDate(new Date());
                
                if (mod.isDebug())
                    mod.getLog().debug("Saving BlockState: " + s.toString());
                
                mod.getModel().setState(s);
            }
        } else if (event.getRightClicked() instanceof ArmorStand) {
            if (mod.getModel().isRestricted(event.getRightClicked().getLocation().getBlock())) {
                if (mod.isDebug())
                    mod.getLog().debug("Modifying standing: " + event.getRightClicked().getLocation().toString());

                if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    if (mod.isDebug())
                        mod.getLog().debug("... was placed by creative. Modify prevented");
                    event.setCancelled(true);
                }
            } else {
                BlockState s = new BlockState();
                s.setLocation(event.getRightClicked().getLocation().getBlock().getLocation());
                s.setPlayer(event.getPlayer());
                s.setDate(new Date());

                if (mod.isDebug())
                    mod.getLog().debug("Saving BlockState: " + s.toString());

                mod.getModel().setState(s);
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        onPlayerInteractEntity(event);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onArmorStandManipulateEvent(PlayerArmorStandManipulateEvent event) {
        onPlayerInteractEntity(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerLeftInteractEntity(EntityDamageByEntityEvent event) {
        if (mod.getConfig().getIgnoredWorlds().contains(event.getEntity().getWorld().getName()))
            return;
        if (event.getDamager() instanceof Player && event.getEntity() instanceof ItemFrame) {
            if (mod.getModel().isRestricted(event.getEntity().getLocation().getBlock())) {
                if (mod.isDebug())
                    mod.getLog().debug("Modifying hanging: " + event.getEntity().getLocation().toString());
                
                if (((Player) event.getDamager()).getGameMode() != GameMode.CREATIVE) {
                    if (mod.isDebug())
                        mod.getLog().debug("... was placed by creative. Modify prevented");
                    event.setCancelled(true);
                }
            } else {
                BlockState s = new BlockState();
                s.setLocation(event.getEntity().getLocation().getBlock().getLocation());
                s.setPlayer((Player) event.getDamager());
                s.setDate(new Date());
                
                if (mod.isDebug())
                    mod.getLog().debug("Saving BlockState: " + s.toString());
                
                mod.getModel().setState(s);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (mod.getConfig().getIgnoredWorlds().contains(event.getEntity().getWorld().getName()))
            return;
        if (event.getEntity() instanceof ItemFrame) {
            if (mod.isDebug())
                mod.getLog().debug("Breaking hanging: " + event.getEntity().getLocation().toString());
            
            if (mod.getModel().isRestricted(event.getEntity().getLocation().getBlock())) {
                if (mod.isDebug())
                    mod.getLog().debug("... was placed by creative. Drop prevented");
                
                mod.getBlockSpawn().block(event.getEntity().getLocation().getBlock().getLocation(), Material.ITEM_FRAME);
                mod.getBlockSpawn().block(event.getEntity().getLocation().getBlock().getLocation(), ((ItemFrame) event.getEntity()).getItem().getType());
            }
            
            mod.getModel().removeState(event.getEntity().getLocation().getBlock());
        }
    }

    protected void checkArmoryDestroy(ArmorStand entity, boolean deRemove) {
        if (mod.isDebug())
            mod.getLog().debug("Breaking standing: " + entity.getLocation().toString());

        if (mod.getModel().isRestricted(entity.getLocation().getBlock())) {
            if (mod.isDebug())
                mod.getLog().debug("... was placed by creative. Drop prevented");


            mod.getBlockSpawn().block(entity.getLocation().getBlock().getLocation(), Material.ARMOR_STAND);
            mod.getBlockSpawn().block(entity.getLocation().getBlock().getRelative(BlockFace.UP).getLocation(), Material.ARMOR_STAND);

            mod.getBlockSpawn().block(entity.getLocation().getBlock().getLocation(), entity.getHelmet());
            mod.getBlockSpawn().block(entity.getLocation().getBlock().getLocation(), entity.getChestplate());
            mod.getBlockSpawn().block(entity.getLocation().getBlock().getLocation(), entity.getBoots());
            mod.getBlockSpawn().block(entity.getLocation().getBlock().getLocation(), entity.getItemInHand());
            mod.getBlockSpawn().block(entity.getLocation().getBlock().getLocation(), entity.getLeggings());

            mod.getBlockSpawn().block(entity.getLocation().getBlock().getRelative(BlockFace.UP).getLocation(), entity.getHelmet());
            mod.getBlockSpawn().block(entity.getLocation().getBlock().getRelative(BlockFace.UP).getLocation(), entity.getChestplate());
            mod.getBlockSpawn().block(entity.getLocation().getBlock().getRelative(BlockFace.UP).getLocation(), entity.getBoots());
            mod.getBlockSpawn().block(entity.getLocation().getBlock().getRelative(BlockFace.UP).getLocation(), entity.getItemInHand());
            mod.getBlockSpawn().block(entity.getLocation().getBlock().getRelative(BlockFace.UP).getLocation(), entity.getLeggings());
            /*entity.setBoots(null);
            entity.setChestplate(null);
            entity.setHelmet(null);
            entity.setItemInHand(null);
            entity.setLeggings(null);*/
        }

        if (deRemove)
            mod.getModel().removeState(entity.getLocation().getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStandingBreak(EntityDeathEvent event) {
        if (mod.getConfig().getIgnoredWorlds().contains(event.getEntity().getWorld().getName()))
            return;
        if (event.getEntity() instanceof ArmorStand) {
            checkArmoryDestroy((ArmorStand) event.getEntity(), true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStandingBreaking(EntityDamageByEntityEvent event) {
        if (mod.getConfig().getIgnoredWorlds().contains(event.getEntity().getWorld().getName()))
            return;
        if (event.getEntity() instanceof ArmorStand) {
            // TODO: Bug in Spigot, we can not check for destroying yet. so the block state stays in DB :(
            //if (event.getEntity().isDead() || ((ArmorStand) event.getEntity()).getHealth() <= event.getFinalDamage()) {
                checkArmoryDestroy((ArmorStand) event.getEntity(), false);
            //}
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        if (mod.getConfig().getIgnoredWorlds().contains(event.getEntity().getWorld().getName()))
            return;
        if (event.getEntity() instanceof ItemFrame) {
            BlockState s = new BlockState();
            s.setLocation(event.getEntity().getLocation().getBlock().getLocation());
            s.setPlayer(event.getPlayer());
            s.setDate(new Date());
            if (mod.isDebug())
                mod.getLog().debug("Saving BlockState: " + s.toString());
            
            mod.getModel().setState(s);
        }
    }
}
