package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.util.Date;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import de.jaschastarke.minecraft.limitedcreative.ModBlockStates;

public class HangingListener implements Listener {
    private ModBlockStates mod;
    public HangingListener(ModBlockStates mod) {
        this.mod = mod;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ItemFrame) {
            if (mod.getModel().isRestricted(event.getRightClicked().getLocation().getBlock())) {
                if (mod.isDebug())
                    mod.getLog().debug("Modifying hanging: " + event.getRightClicked().getLocation().toString());
                
                if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    if (mod.isDebug())
                        mod.getLog().debug("... was placed by creative. Modify prevented");
                    event.setCancelled(true);
                    return;
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
    public void onHangingBreak(HangingBreakEvent event) {
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
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
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
