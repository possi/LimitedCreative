package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.sql.SQLException;
import java.util.Date;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import de.jaschastarke.minecraft.limitedcreative.ModBlockStates;

public class BlockListener implements Listener {
    private ModBlockStates mod;
    public BlockListener(ModBlockStates mod) {
        this.mod = mod;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;

        try {
            BlockState s = mod.getQueries().find(event.getBlock().getLocation());
            if (s != null) {
                if (mod.isDebug())
                    mod.getLog().debug("Breaking bad, err.. block: " + s.toString());
                
                if (s.getGameMode() == GameMode.CREATIVE && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    if (mod.isDebug())
                        mod.getLog().debug("... was placed by creative. Drop prevented");
                    mod.getBlockSpawn().block(event.getBlock(), event.getPlayer());
                }
                
                mod.getQueries().delete(s);
            }
        } catch (SQLException e) {
            mod.getLog().warn("DB-Error while onBlockBreak: "+e.getMessage());
            event.setCancelled(true);
        }
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;
        
        try {
            BlockState s = mod.getQueries().find(event.getBlock().getLocation());
            if (s != null) {
                // This shouldn't happen
                if (mod.isDebug())
                    mod.getLog().debug("Replacing current BlockState: " + s.toString());
            } else {
                s = new BlockState();
                s.setLocation(event.getBlock().getLocation());
            }
            s.setPlayer(event.getPlayer());
            s.setDate(new Date());
            if (mod.isDebug())
                mod.getLog().debug("Saving BlockState: " + s.toString());
            mod.getQueries().insert(s);
        } catch (SQLException e) {
            mod.getLog().warn("DB-Error while onBlockPlace: "+e.getMessage());
            event.setCancelled(true);
        }
    }
}
