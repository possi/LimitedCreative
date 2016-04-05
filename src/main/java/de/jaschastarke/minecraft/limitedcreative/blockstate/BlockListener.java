package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import de.jaschastarke.bukkit.lib.events.BlockDestroyedEvent;
import de.jaschastarke.bukkit.lib.events.BlockMovedEvent;
import de.jaschastarke.minecraft.limitedcreative.ModBlockStates;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBModel.DBTransaction;

public class BlockListener implements Listener {
    private final static String FALLING_ENTITY_BSMDKEY = "blockstate";
    private ModBlockStates mod;
    private MetadataValue blockAlreadExtended;
    
    public BlockListener(ModBlockStates mod) {
        this.mod = mod;
        blockAlreadExtended = new FixedMetadataValue(mod.getPlugin(), new Boolean(true));
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (mod.getConfig().getIgnoredWorlds().contains(event.getBlock().getWorld().getName()))
            return;
        if (mod.getModel().isRestricted(event.getBlock())) {
            if (mod.isDebug())
                mod.getLog().debug("Breaking bad, err.. block: " + event.getBlock().getLocation().toString() + " was placed by creative. Drop prevented");
            
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                mod.getBlockSpawn().block(event.getBlock(), event.getPlayer());
                event.setExpToDrop(0);
            }
        }
        mod.getModel().removeState(event.getBlock());
    }
    
    @EventHandler
    public void onOtherBlockDestroy(BlockDestroyedEvent event) {
        if (mod.getConfig().getIgnoredWorlds().contains(event.getBlock().getWorld().getName()))
            return;
        if (mod.getModel().isRestricted(event.getBlock())) {
            if (mod.isDebug())
                mod.getLog().debug("Breaking attached block: " + event.getBlock().getLocation().toString() + " was placed by creative. Drop prevented");
            
            mod.getBlockSpawn().block(event.getBlock());
        }
        mod.getModel().removeState(event.getBlock());
    }
    
    @EventHandler
    public void onBlockMoved(BlockMovedEvent event) {
        if (mod.getConfig().getIgnoredWorlds().contains(event.getBlock().getWorld().getName()))
            return;
        for (MetadataValue md : event.getEntity().getMetadata(FALLING_ENTITY_BSMDKEY)) {
            if (md.value() instanceof BlockState) {
                BlockState bs = (BlockState) md.value();
                // The state of the source block should be always be cached yet, as we either asked it before, or
                // it was just placed
                if (bs != mod.getModel().getState(event.getSource())) {
                    bs.setLocation(event.getBlock().getLocation());
                    mod.getModel().setState(bs);
                    return;
                }
            }
        }
        mod.getModel().moveState(event.getSource(), event.getBlock());
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(final EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.FALLING_BLOCK) {
            if (event.getTo() == Material.AIR) {
                if (mod.getModel().isRestricted(event.getBlock())) {
                    if (mod.isDebug())
                        mod.getLog().debug("Falling block: " + event.getBlock().getLocation().toString() + " was placed by creative (drop prevented)");
                    FallingBlock fe = (FallingBlock) event.getEntity();
                    fe.setDropItem(false);
                    // Using getState to fetch the full state from database synchronous isn't optimal, but either it is
                    // cached, as it was just placed, or it isn't that important, as it is a rare event
                    fe.setMetadata(FALLING_ENTITY_BSMDKEY, new FixedMetadataValue(mod.getPlugin(), mod.getModel().getState(event.getBlock())));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlocksBreakByExplosion(EntityExplodeEvent event) {
        if (mod.getConfig().getIgnoredWorlds().contains(event.getLocation().getWorld().getName()))
            return;
        Map<Block, Boolean> states = mod.getModel().getRestrictedStates(event.blockList());
        DBTransaction update = mod.getModel().groupUpdate();
        for (Block block : event.blockList()) {
            if (mod.isDebug())
                mod.getLog().debug("Breaking bad, err.. block by explosion: " + block.getLocation().toString());
            
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
        if (mod.getConfig().getIgnoredWorlds().contains(event.getBlock().getWorld().getName()))
            return;
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
        if (mod.getConfig().getIgnoredWorlds().contains(event.getBlock().getWorld().getName()))
            return;
        if (event.getBlock().getMetadata("LCBS_pistonIsAlreadyExtended").size() > 0) // Fixes long known Bukkit issue
            return;
        event.getBlock().setMetadata("LCBS_pistonIsAlreadyExtended", blockAlreadExtended);
        
        /*if (mod.isDebug())
            mod.getLog().debug("PistonExtend "+source.getType()+" "+source.getLocation()+" "+event.getDirection());*/
        
        List<Block> movedBlocks = event.getBlocks();
        
        if (movedBlocks.size() > 0) {
            DBTransaction update = mod.getModel().groupUpdate();
            for(int count = movedBlocks.size()-1; count >= 0; count--){
        	Block sblock = movedBlocks.get(count);
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
        if (mod.getConfig().getIgnoredWorlds().contains(event.getBlock().getWorld().getName()))
            return;
        event.getBlock().removeMetadata("LCBS_pistonIsAlreadyExtended", mod.getPlugin());
        
        List<Block> movedBlocks = event.getBlocks();
        if(movedBlocks.size() > 0)
        {
            DBTransaction update = mod.getModel().groupUpdate();
            for(int count = movedBlocks.size()-1; count >= 0; count--){
        	Block sblock = movedBlocks.get(count);
        	Block dest = sblock.getRelative(event.getDirection());
        	if (mod.isDebug())
        	    mod.getLog().debug("PistionRetract moves "+sblock.getType()+"-Block from "+sblock.getLocation()+" to "+dest.getLocation());
        	
        	update.moveState(sblock, dest);
            }
            update.finish();
        }
    }
}
