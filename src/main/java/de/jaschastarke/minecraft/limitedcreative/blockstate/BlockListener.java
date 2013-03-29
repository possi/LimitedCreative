package de.jaschastarke.minecraft.limitedcreative.blockstate;

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
        
        BlockLocation bl = new BlockLocation(event.getBlock().getLocation());
        BlockState s = mod.getDB().find(BlockState.class, bl);
        if (s != null) {
            if (mod.isDebug())
                mod.getLog().debug("Breaking bad, err.. block: " + s.toString());
            
            if (s.getGameMode() == GameMode.CREATIVE && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                mod.getBlockSpawn().block(event.getBlock(), event.getPlayer());
            }
            
            mod.getDB().delete(s);
        }
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;
        
        BlockLocation bl = new BlockLocation(event.getBlock().getLocation());
        BlockState s = mod.getDB().find(BlockState.class, bl);
        if (s != null) {
            // This shouldn't happen
            if (mod.isDebug())
                mod.getLog().debug("Replacing current BlockState: " + s.toString());
        } else {
            s = new BlockState();
            s.setBlockLocation(bl);
        }
        s.setPlayer(event.getPlayer());
        s.setDate(new Date());
        if (mod.isDebug())
            mod.getLog().debug("Saving BlockState: " + s.toString());
        mod.getDB().save(s);
    }
}
