package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

import de.jaschastarke.database.DatabaseConfigurationException;
import de.jaschastarke.minecraft.limitedcreative.ModBlockStates;
import de.jaschastarke.minecraft.limitedcreative.blockstate.thread.ThreadLink;
import de.jaschastarke.minecraft.limitedcreative.blockstate.thread.Transaction;

public class ThreadedModel extends AbstractModel implements DBModel, Listener {
    private ModBlockStates mod;
    private ThreadLink threads;
    private MetadataValue metadataSet;
    private MetadataValue metadataSetRestricted;

    public ThreadedModel(ModBlockStates mod) {
        super(mod.getPlugin());
        this.mod = mod;
        metadataSet = new FixedMetadataValue(mod.getPlugin(), new Boolean(true));
        metadataSetRestricted = new FixedMetadataValue(mod.getPlugin(), new Object());
    }

    @Override
    public void onEnable() throws SQLException, DatabaseConfigurationException {
        DBQueries queries = new DBQueries(mod, mod.getPlugin().getDatabaseConnection());
        queries.initTable();
        threads = new ThreadLink(this, queries);
        // We don't keep any reference to queries, because it contains the DB-Connection, which should be only used
        // from the thread from now on (as SQLite may not threadsafe)
        for (World w : mod.getPlugin().getServer().getWorlds()) {
            if (!mod.getConfig().getIgnoredWorlds().containsIgnoreCase(w.getName())) {
                for (Chunk chunk : w.getLoadedChunks()) {
                    threads.queueChunkLoad(chunk);
                }
            }
        }
        threads.start();
    }

    @Override
    public void onDisable() {
        try {
            threads.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
            mod.getLog().severe("Failed to clean end Database-Thread, maybe BlockStates haven't been saved");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!mod.getConfig().getIgnoredWorlds().containsIgnoreCase(event.getWorld().getName())) {
            threads.queueChunkLoad(event.getChunk());
        }
    }

    @Override
    public void moveState(Block from, Block to) {
        threads.queueMetaMove(from.getLocation(), to.getLocation());
        moveMetaState(from, to);
    }

    @Override
    public void removeState(BlockState state) {
        removeState(state.getLocation().getBlock());
    }

    @Override
    public void removeState(Block block) {
        setMetaBlock(block, null);
        threads.queueUpdate(block);
    }


    @Override
    public Map<Block, Boolean> getRestrictedStates(List<Block> blocks) {
        Map<Block, Boolean> ret = new HashMap<Block, Boolean>();
        for (Block block : blocks) {
            HasBlockState has = getMetaBlock(block);
            ret.put(block, has.restricted);
        }
        return ret;
    }
    
    @Override
    public Map<Block, BlockState> getStates(List<Block> blocks) {
        Map<Block, BlockState> ret = new HashMap<Block, BlockState>();
        Cuboid c;
        do {
            c = new Cuboid();
            for (Block block : blocks) {
                HasBlockState has = getMetaBlock(block);
                if (has.set) {
                    ret.put(block, has.state);
                } else {
                    c.add(block.getLocation());
                    ret.put(block, null);
                }
            }
            if (!c.isEmpty())
                threads.callUpdate(c);
        } while(!c.isEmpty());
        return ret;
    }

    @Override
    public void cacheStates(Cuboid c) {
        threads.callUpdate(c);
    }

    @Override
    public BlockState getState(Block block) {
        HasBlockState has = getMetaBlock(block);
        if (!has.set || (has.dbSet && has.state == null)) {
            return threads.callUpdate(block);
        }
        return has.state;
    }
    @Override
    public boolean isRestricted(Block block) {
        return getMetaBlock(block).restricted;
    }

    @Override
    public void setState(BlockState state) {
        Block block = state.getLocation().getBlock();
        boolean store = state.isRestricted() || mod.getConfig().getLogSurvival();
        
        setMetaBlock(block, store ? state : null);
        
        threads.queueUpdate(block);
    }

    @Override
    public DBTransaction groupUpdate() {
        return new GroupUpdate(threads);
    }
    
    private class GroupUpdate extends Transaction {
        public GroupUpdate(ThreadLink threads) {
            super(threads);
        }

        @Override
        public void moveState(Block from, Block to) {
            moveMetaState(from, to);
            
            super.moveState(from, to);
        }

        @Override
        public void setState(BlockState state) {
            Block block = state.getLocation().getBlock();
            boolean store = state.isRestricted() || mod.getConfig().getLogSurvival();
            
            setMetaBlock(block, store ? state : null);
            
            super.setState(state);
        }

        @Override
        public void removeState(Block block) {
            setMetaBlock(block, null);
            
            super.setState(block);
        }
    }
    
    /**
     * Metadata-Interface for the Thread-Link
     */
    public HasBlockState getMetaState(Block block) {
        return getMetaBlock(block);
    }
    /**
     * Metadata-Interface for the Thread-Link
     */
    public void setMetaState(Block block, BlockState state) {
        super.setMetaBlock(block, state);
    }
    public void setSimpleMetaDataState(Block block, BlockState state) {
        if (state == null)
            super.setMetaBlock(block, null);
        else if (state.isRestricted())
            block.setMetadata(BSMDKEY, metadataSetRestricted);
        else
            block.setMetadata(BSMDKEY, metadataSet);
    }
    protected HasBlockState getMetaBlock(Metadatable m) {
        HasBlockState has = new HasBlockState();
        List<MetadataValue> metadata = m.getMetadata(BSMDKEY);
        for (MetadataValue v : metadata) {
            if (v.value() instanceof BlockState) {
                has.set = true;
                has.state = (BlockState) v.value();
                has.dbSet = true;
                has.restricted = has.state.isRestricted();
                break;
            } else if (v.getOwningPlugin() == mod.getPlugin()) {
                if (v == metadataNull) {
                    has.set = true;
                    has.state = null;
                } else if (v == metadataSet) {
                    has.set = true;
                    has.state = null;
                    has.dbSet = true;
                } else if (v == metadataSetRestricted) {
                    has.set = true;
                    has.state = null;
                    has.dbSet = true;
                    has.restricted = true;
                }
                break;
            }
        }
        return has;
    }
    public static class HasBlockState extends AbstractModel.HasBlockState {
        public boolean dbSet = false;
        public boolean restricted = false;
    }
    
    public ModBlockStates getModel() {
        return mod;
    }
}
