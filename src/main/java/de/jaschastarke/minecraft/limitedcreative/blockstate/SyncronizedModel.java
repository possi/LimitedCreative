package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.block.Block;

import de.jaschastarke.database.DatabaseConfigurationException;
import de.jaschastarke.minecraft.limitedcreative.ModBlockStates;

/**
 * @internal I'm not happy with the error-handling here, especially in the Transaction, but I'll focus on the asynchronous
 *  variant, where all errors have to be handled in a separate thread.
 */
public class SyncronizedModel extends AbstractModel implements DBModel {
    private ModBlockStates mod;
    private DBQueries q;

    public SyncronizedModel(ModBlockStates mod) throws DatabaseConfigurationException {
        super(mod.getPlugin());
        this.mod = mod;
        this.q = new DBQueries(mod, mod.getPlugin().getDatabaseConnection());
    }

    @Override
    public void onEnable() throws SQLException {
        this.q.initTable();
    }
    @Override
    public void onDisable() {
    }
    
    public void moveState(Block from, Block to) {
        try {
            q.delete(to.getLocation());
            q.move(from.getLocation(), to.getLocation());
        } catch (SQLException e) {
            mod.getLog().severe(e.getMessage());
            mod.getLog().warn("Failed to move BlockState in DB from " + from.getLocation().toString() + " to " + to.getLocation().toString());
        }
        moveMetaState(from, to);
    }
    public void removeState(BlockState state) {
        removeState(state.getLocation().getBlock());
    }

    @Override
    public void removeState(Block block) {
        setMetaBlock(block, null);
        try {
            q.delete(block.getLocation());
        } catch (SQLException e) {
            mod.getLog().severe(e.getMessage());
            mod.getLog().warn("Failed to delete BlockState in DB from " + block.getLocation().toString());
        }
    }

    @Override
    public Map<Block, Boolean> getRestrictedStates(List<Block> blocks) {
        Map<Block, Boolean> ret = new HashMap<Block, Boolean>();
        for (Map.Entry<Block, BlockState> entry : getStates(blocks).entrySet()) {
            ret.put(entry.getKey(), entry.getValue().isRestricted());
        }
        return ret;
    }
    public Map<Block, BlockState> getStates(List<Block> blocks) {
        Map<Block, BlockState> ret = new HashMap<Block, BlockState>();
        
        Cuboid c = new Cuboid();
        for (Block block : blocks) {
            HasBlockState has = getMetaBlock(block);
            if (has.isSet()) {
                ret.put(block, has.getState());
            } else {
                c.add(block.getLocation());
            }
        }
        if (!c.isEmpty()) {
            try {
                List<BlockState> dbb = q.findAllIn(c);
                for (BlockState bs : dbb) {
                    setMetaBlock(bs.getLocation().getBlock(), bs);
                    if (blocks.contains(bs.getLocation().getBlock()))
                        ret.put(bs.getLocation().getBlock(), bs);
                }
                for (Block block : blocks) {
                    if (!ret.containsKey(block)) {
                        ret.put(block, null);
                        setMetaBlock(block, null);
                    }
                }
            } catch (SQLException e) {
                mod.getLog().severe(e.getMessage());
                mod.getLog().warn("Failed to fetch BlockState from DB in " + c.toString());
            }
        }
        return ret;
    }
    public void cacheStates(Cuboid c) {
        if (!c.isEmpty()) {
            try {
                List<BlockState> dbb = q.findAllIn(c);
                for (BlockState bs : dbb) {
                    setMetaBlock(bs.getLocation().getBlock(), bs);
                }
            } catch (SQLException e) {
                mod.getLog().severe(e.getMessage());
                mod.getLog().warn("Failed to fetch BlockState (for caching) from DB in " + c.toString());
            }
        }
    }

    @Override
    public boolean isRestricted(Block block) {
        BlockState state = getState(block);
        return state != null ? state.isRestricted() : false;
    }
    
    public BlockState getState(Block block) {
        HasBlockState has = getMetaBlock(block);
        if (!has.isSet()) {
            try {
                BlockState state = q.find(block.getLocation());
                setMetaBlock(block, state);
                return state;
            } catch (SQLException e) {
                mod.getLog().severe(e.getMessage());
                mod.getLog().warn("Failed to fetch BlockState (for caching) from DB at " + block.getLocation().toString());
                return null;
            }
        }
        return has.getState();
    }
    public void setState(BlockState state) {
        Block block = state.getLocation().getBlock();
        boolean store = state != null && (state.isRestricted() || mod.getConfig().getLogSurvival());
        
        setMetaBlock(block, store ? state : null);
        
        try {
            if (!store)
                q.delete(state);
            else if (!q.update(state))
                q.insert(state);
        } catch (SQLException e) {
            mod.getLog().severe(e.getMessage());
            mod.getLog().warn("Failed to update BlockState in DB at " + block.getLocation().toString());
        }
    }
    
    @Override
    public DBTransaction groupUpdate() {
        return new Transaction();
    }
    
    private class Transaction implements DBTransaction {
        private boolean finished = false;
        private Transaction() {
            try {
                q.getDB().startTransaction();
            } catch (SQLException e) {
                mod.getLog().severe(e.getMessage());
                finished = true;
            }
        }
        
        @Override
        public void moveState(Block from, Block to) {
            if (finished)
                throw new IllegalAccessError("Transaction already ended");
            SyncronizedModel.this.moveState(from, to);
        }
        
        @Override
        public void setState(BlockState state) {
            if (finished)
                throw new IllegalAccessError("Transaction already ended");
            SyncronizedModel.this.setState(state);
        }

        @Override
        public void removeState(Block block) {
            if (finished)
                throw new IllegalAccessError("Transaction already ended");
            SyncronizedModel.this.removeState(block);
        }
        
        @Override
        public void finish() {
            try {
                q.getDB().endTransaction();
            } catch (SQLException e) {
                mod.getLog().severe(e.getMessage());
            } finally {
                finished = true;
            }
        }
    }

    @Override
    public int cleanUp(Cleanup target) {
        try {
            return q.cleanup(target);
        } catch (SQLException e) {
            mod.getLog().severe(e.getMessage());
            mod.getLog().warn("Failed to cleanup BlockState-DB");
            return -1;
        }
    }
}
