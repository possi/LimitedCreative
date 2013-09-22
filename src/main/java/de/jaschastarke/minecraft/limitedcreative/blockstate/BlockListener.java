package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.sql.SQLException;
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

public class BlockListener implements Listener {
    private ModBlockStates mod;
    public BlockListener(ModBlockStates mod) {
        this.mod = mod;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        try {
            BlockState s = mod.getModel().getState(event.getBlock());
            if (s != null) {
                if (mod.isDebug())
                    mod.getLog().debug("Breaking bad, err.. block: " + s.toString());
                
                if (s.isRestricted() && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    if (mod.isDebug())
                        mod.getLog().debug("... was placed by creative. Drop prevented");
                    mod.getBlockSpawn().block(event.getBlock(), event.getPlayer());
                    event.setExpToDrop(0);
                }
                
                mod.getModel().removeState(s);
            }
        } catch (SQLException e) {
            mod.getLog().warn("DB-Error while onBlockBreak: "+e.getMessage());
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onOtherBlockDestroy(BlockDestroyedEvent event) {
        try {
            BlockState s = mod.getModel().getState(event.getBlock());
            if (s != null) {
                if (mod.isDebug())
                    mod.getLog().debug("Breaking attached block: " + s.toString());
                
                if (s.isRestricted()) {
                    if (mod.isDebug())
                        mod.getLog().debug("... was placed by creative. Drop prevented");
                    mod.getBlockSpawn().block(event.getBlock());
                }
                
                mod.getModel().removeState(s);
            }
        } catch (SQLException e) {
            mod.getLog().warn("DB-Error while onBlockBreak: "+e.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlocksBreakByExplosion(EntityExplodeEvent event) {
        try {
            Map<Block, BlockState> states = mod.getModel().getStates(event.blockList());
            for (Block block : event.blockList()) {
                BlockState s = states.get(block);
                if (s != null) {
                    if (mod.isDebug())
                        mod.getLog().debug("Breaking bad, err.. block: " + s.toString());
                    
                    if (s.isRestricted()) {
                        if (mod.isDebug())
                            mod.getLog().debug("... was placed by creative. Drop prevented");
                        mod.getBlockSpawn().block(block);
                    }
                    
                    mod.getModel().removeState(s);
                }
            }
        } catch (SQLException e) {
            mod.getLog().warn("DB-Error while onBlockBreakByExplosion: "+e.getMessage());
            event.setCancelled(true);
        }
    }
    
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        try {
            BlockState s = mod.getModel().getState(event.getBlock());
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
            
            mod.getModel().setState(s);
        } catch (SQLException e) {
            mod.getLog().warn("DB-Error while onBlockPlace: "+e.getMessage());
            event.setCancelled(true);
        }
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
        
        try {
            if (movedBlocks.size() > 0) {
                mod.getQueries().getDB().startTransaction();
                for (Block sblock : movedBlocks) {
                    Block dest = sblock.getRelative(event.getDirection());
                    if (mod.isDebug())
                        mod.getLog().debug("PistionExtend moves "+sblock.getType()+"-Block from "+sblock.getLocation()+" to "+dest.getLocation());
                    
                    mod.getModel().moveState(sblock, dest);
                }
                mod.getQueries().getDB().endTransaction();
            }
        } catch (SQLException e) {
            try {
                mod.getQueries().getDB().revertTransaction();
            } catch (SQLException e1) {
            }
            mod.getLog().warn("DB-Error while onBlockMove (extend): "+e.getMessage());
            //event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistionRetract(BlockPistonRetractEvent event) {
        event.getBlock().removeMetadata("LCBS_pistonIsAlreadyExtended", mod.getPlugin());
        
        Block dest = event.getBlock().getRelative(event.getDirection());
        Block source = dest.getRelative(event.getDirection());
        if (event.isSticky() && source.getType() != Material.AIR) {
            try {
                if (mod.isDebug())
                    mod.getLog().debug("PistionRetract moves "+source.getType()+"-Block from "+source.getLocation()+" to "+dest.getLocation());
                mod.getModel().moveState(source, source.getRelative(event.getDirection().getOppositeFace()));
            } catch (SQLException e) {
                try {
                    mod.getQueries().getDB().revertTransaction();
                } catch (SQLException e1) {
                }
                mod.getLog().warn("DB-Error while onBlockMove (retract): "+e.getMessage());
                //event.setCancelled(true);
            }
        }
    }
}
