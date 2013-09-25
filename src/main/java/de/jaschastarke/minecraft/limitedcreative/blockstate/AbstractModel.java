package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;

public abstract class AbstractModel {
    protected static final String BSMDKEY = "blockstate";
    protected MetadataValue metadataNull;
    private Plugin plugin;
    
    protected AbstractModel(Plugin plugin) {
        this.plugin = plugin;
        metadataNull = new FixedMetadataValue(plugin, new Boolean(null));
    }
    
    protected void moveMetaState(Block from, Block to) {
        HasBlockState metaBlock = getMetaBlock(from);
        if (metaBlock.set && metaBlock.state != null) {
            BlockState state = metaBlock.state;
            state.setLocation(to.getLocation());
            setMetaBlock(to, state);
        } else {
            removeMetaBlock(to);
        }
        setMetaBlock(from, null);
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
            m.setMetadata(BSMDKEY, metadataNull);
        else
            m.setMetadata(BSMDKEY, new FixedMetadataValue(plugin, s));
    }
    protected HasBlockState getMetaBlock(Metadatable m) {
        HasBlockState has = new HasBlockState();
        List<MetadataValue> metadata = m.getMetadata(BSMDKEY);
        for (MetadataValue v : metadata) {
            if (v.value() instanceof BlockState) {
                has.set = true;
                has.state = (BlockState) v.value();
                break;
            } else if (v == metadataNull) {
                has.set = true;
                has.state = null;
                break;
            }
        }
        return has;
    }
    protected void removeMetaBlock(Metadatable m) {
        m.removeMetadata(BSMDKEY, plugin);
    }
    
    public static class HasBlockState {
        public boolean set = false;
        public BlockState state = null;
    }

}
