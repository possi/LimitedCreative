package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

import de.jaschastarke.minecraft.limitedcreative.ModBlockStates;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBQueries.Cuboid;

public class DBModel {
    private static final String BSMDKEY = "blockstate";
    private ModBlockStates mod;
    private DBQueries q;

    public DBModel(ModBlockStates mod) {
        this.mod = mod;
        this.q = mod.getQueries();
    }
    
    public void moveState(Block from, Block to) throws SQLException {
        q.delete(to.getLocation());
        q.move(from.getLocation(), to.getLocation());
        HasBlockState metaBlock = getMetaBlock(from);
        if (metaBlock.set && metaBlock.state != null) {
            BlockState state = metaBlock.state;
            state.setLocation(to.getLocation());
            setMetaBlock(to, getMetaBlock(from).state);
        } else {
            removeMetaBlock(to);
        }
        setMetaBlock(from, null);
    }
    public void removeState(BlockState state) throws SQLException {
        //removeMetaBlock(state.getLocation().getBlock());
        setMetaBlock(state.getLocation().getBlock(), null);
        q.delete(state);
    }
    public Map<Block, BlockState> getStates(List<Block> blocks) throws SQLException {
        Map<Block, BlockState> ret = new HashMap<Block, BlockState>();
        
        Cuboid c = new Cuboid();
        for (Block block : blocks) {
            HasBlockState has = getMetaBlock(block);
            if (has.set) {
                ret.put(block, has.state);
            } else {
                c.add(block.getLocation());
            }
        }
        if (!c.isEmpty()) {
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
        }
        /*for (Block block : blocks) {
            if (ret.containsKey(block))
                ret.put(block, getState(block));
        }*/
        return ret;
    }
    public void cacheStates(Cuboid c) throws SQLException {
        if (!c.isEmpty()) {
            List<BlockState> dbb = q.findAllIn(c);
            for (BlockState bs : dbb) {
                setMetaBlock(bs.getLocation().getBlock(), bs);
            }
        }
    }
    public BlockState getState(Block block) throws SQLException {
        HasBlockState has = getMetaBlock(block);
        if (!has.set) {
            BlockState state = q.find(block.getLocation());
            setMetaBlock(block, state);
            return state;
        }
        return has.state;
    }
    public void setState(BlockState state) throws SQLException {
        Block block = state.getLocation().getBlock();
        boolean update = hasMetaBlock(block);
        boolean store = state.isRestricted() || mod.getConfig().getLogSurvival();
        
        setMetaBlock(block, store ? state : null);
        
        if (update) {
            if (!store)
                q.delete(state);
            else if (!q.update(state))
                q.insert(state);
        } else {
            if (store)
                q.insert(state);
        }
    }
    
    protected boolean hasMetaBlock(Metadatable m) {
        List<MetadataValue> metadata = m.getMetadata(BSMDKEY);
        for (MetadataValue v : metadata) {
            if (v.value() instanceof BlockState)
                return true;
        }
        return false;
    }
    protected void setMetaBlock(Metadatable m, BlockState s) {
        if (s == null)
            m.setMetadata(BSMDKEY, new FixedMetadataValue(mod.getPlugin(), new Boolean(false)));
        else
            m.setMetadata(BSMDKEY, new FixedMetadataValue(mod.getPlugin(), s));
    }
    protected HasBlockState getMetaBlock(Metadatable m) {
        HasBlockState has = new HasBlockState();
        List<MetadataValue> metadata = m.getMetadata(BSMDKEY);
        for (MetadataValue v : metadata) {
            if (v.value() instanceof BlockState) {
                has.set = true;
                has.state = (BlockState) v.value();
                break;
            } else if (v.getOwningPlugin() == mod.getPlugin()) {
                has.set = true;
                has.state = null;
                break;
            }
        }
        return has;
    }
    protected void removeMetaBlock(Metadatable m) {
        m.removeMetadata(BSMDKEY, mod.getPlugin());
    }
    
    protected static class HasBlockState {
        public boolean set = false;
        public BlockState state = null;
    }
}
