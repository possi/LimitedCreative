package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.sql.SQLException;
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
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockState.Source;

public class HangingListener implements Listener {
    private ModBlockStates mod;
    public HangingListener(ModBlockStates mod) {
        this.mod = mod;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ItemFrame) {
            try {
                BlockState s = mod.getQueries().find(event.getRightClicked().getLocation());
                if (s != null) {
                    if (mod.isDebug())
                        mod.getLog().debug("Modifying hanging: " + s.toString());
                    
                    if ((s.getGameMode() == GameMode.CREATIVE || s.getSource() == Source.EDIT) && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                        if (mod.isDebug())
                            mod.getLog().debug("... was placed by creative. Modify prevented");
                        event.setCancelled(true);
                        return;
                    } else {
                        s.setPlayer(event.getPlayer());
                        s.setDate(new Date());
                        mod.getQueries().update(s);
                    }
                } else {
                    s = new BlockState();
                    s.setLocation(event.getRightClicked().getLocation().getBlock().getLocation());
                    s.setPlayer(event.getPlayer());
                    s.setDate(new Date());
                    
                    if (mod.isDebug())
                        mod.getLog().debug("Saving BlockState: " + s.toString());
                    
                    mod.getQueries().insert(s);
                }
            } catch (SQLException e) {
                mod.getLog().warn("DB-Error while onHangingInteract: "+e.getMessage());
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (event.getEntity() instanceof ItemFrame) {
            try {
                BlockState s = mod.getQueries().find(event.getEntity().getLocation());
                if (s != null) {
                    if (mod.isDebug())
                        mod.getLog().debug("Breaking hanging: " + s.toString());
                    
                    if (s.getGameMode() == GameMode.CREATIVE || s.getSource() == Source.EDIT) {
                        if (mod.isDebug())
                            mod.getLog().debug("... was placed by creative. Drop prevented");
                        //mod.getBlockSpawn().block(event.getEntity().getLocation().getBlock().getLocation());
                        mod.getBlockSpawn().block(event.getEntity().getLocation().getBlock().getLocation(), Material.ITEM_FRAME);
                        mod.getBlockSpawn().block(event.getEntity().getLocation().getBlock().getLocation(), ((ItemFrame) event.getEntity()).getItem().getType());
                    }
                    
                    mod.getQueries().delete(s);
                }
            } catch (SQLException e) {
                mod.getLog().warn("DB-Error while onHangingBreak: "+e.getMessage());
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        if (event.getEntity() instanceof ItemFrame) {
            try {
                BlockState s = mod.getQueries().find(event.getEntity().getLocation());
                boolean update = false;
                if (s != null) {
                    // This shouldn't happen
                    if (mod.isDebug())
                        mod.getLog().debug("Replacing current BlockState: " + s.toString());
                    update = true;
                } else {
                    s = new BlockState();
                    s.setLocation(event.getEntity().getLocation().getBlock().getLocation());
                }
                s.setPlayer(event.getPlayer());
                s.setDate(new Date());
                if (mod.isDebug())
                    mod.getLog().debug("Saving BlockState: " + s.toString());
                
                if (update)
                    mod.getQueries().update(s);
                else
                    mod.getQueries().insert(s);
            } catch (SQLException e) {
                mod.getLog().warn("DB-Error while onHangingPlace: "+e.getMessage());
                event.setCancelled(true);
            }
        }
    }
}
