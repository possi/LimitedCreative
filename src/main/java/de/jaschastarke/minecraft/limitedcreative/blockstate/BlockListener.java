package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.FixedMetadataValue;

import de.jaschastarke.bukkit.lib.events.BlockDestroyedEvent;
import de.jaschastarke.minecraft.limitedcreative.ModBlockStates;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBModel.DBTransaction;

public class BlockListener implements Listener {
    private ModBlockStates mod;
    public BlockListener(ModBlockStates mod) {
        this.mod = mod;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (mod.getModel().isRestricted(event.getBlock())) {
            if (mod.isDebug())
                mod.getLog().debug("Breaking bad, err.. block: " + event.getBlock().getLocation().toString());
            
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                if (mod.isDebug())
                    mod.getLog().debug("... was placed by creative. Drop prevented");
                mod.getBlockSpawn().block(event.getBlock(), event.getPlayer());
                event.setExpToDrop(0);
            }
        }
        mod.getModel().removeState(event.getBlock());
    }
    
    @EventHandler
    public void onOtherBlockDestroy(BlockDestroyedEvent event) {
        if (mod.getModel().isRestricted(event.getBlock())) {
            if (mod.isDebug())
                mod.getLog().debug("Breaking attached block: " + event.getBlock().getLocation().toString());
            
            if (mod.isDebug())
                mod.getLog().debug("... was placed by creative. Drop prevented");
            mod.getBlockSpawn().block(event.getBlock());
        }
        mod.getModel().removeState(event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlocksBreakByExplosion(EntityExplodeEvent event) {
        Map<Block, Boolean> states = mod.getModel().getRestrictedStates(event.blockList());
        DBTransaction update = mod.getModel().groupUpdate();
        for (Block block : event.blockList()) {
            if (mod.isDebug())
                mod.getLog().debug("Breaking bad, err.. block: " + block.getLocation().toString());
            
            if (states.get(block)) {
                if (mod.isDebug())
                    mod.getLog().debug("... was placed by creative. Drop prevented");
                mod.getBlockSpawn().block(block);
            }
            
            update.removeState(block);
        }
        update.finish();
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        BlockState s = new BlockState();
        s.setLocation(event.getBlock().getLocation());
        s.setPlayer(event.getPlayer());
        s.setDate(new Date());
        if (mod.isDebug())
            mod.getLog().debug("Saving BlockState: " + s.toString());
        
        mod.getModel().setState(s);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistionExtend(BlockPistonExtendEvent event) {
        if (event.getBlock().getMetadata("LCBS_pistonIsAlreadyExtended").size() > 0) // Fixes long known Bukkit issue
            return;
        event.getBlock().setMetadata("LCBS_pistonIsAlreadyExtended", new FixedMetadataValue(mod.getPlugin(), new Boolean(true)));
        
        Block source = event.getBlock().getRelative(event.getDirection());
        /*if (mod.isDebug())
            mod.getLog().debug("PistonExtend "+source.getType()+" "+source.getLocation()+" "+event.getDirection());*/
        
        List<Block> movedBlocks = new ArrayList<Block>();
        while (source != null && source.getType() != Material.AIR) {
            movedBlocks.add(0, source); // put on top, so iterating the
            source = source.getRelative(event.getDirection());
        }
        
        if (movedBlocks.size() > 0) {
            DBTransaction update = mod.getModel().groupUpdate();
            for (Block sblock : movedBlocks) {
                Block dest = sblock.getRelative(event.getDirection());
                if (mod.isDebug())
                    mod.getLog().debug("PistionExtend moves "+sblock.getType()+"-Block from "+sblock.getLocation()+" to "+dest.getLocation());
                
                update.moveState(sblock, dest);
            }
            update.finish();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistionRetract(BlockPistonRetractEvent event) {
        event.getBlock().removeMetadata("LCBS_pistonIsAlreadyExtended", mod.getPlugin());
        
        Block dest = event.getBlock().getRelative(event.getDirection());
        Block source = dest.getRelative(event.getDirection());
        if (event.isSticky() && source.getType() != Material.AIR) {
            if (mod.isDebug())
                mod.getLog().debug("PistionRetract moves "+source.getType()+"-Block from "+source.getLocation()+" to "+dest.getLocation());
            mod.getModel().moveState(source, source.getRelative(event.getDirection().getOppositeFace()));
        }
    }
}
