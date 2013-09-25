package de.jaschastarke.minecraft.limitedcreative.blockstate.thread;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.block.Block;

import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockState;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBModel.Cuboid;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBQueries;

public class CacheChunkAction implements Action {
    private static final int CHUNK_SIZE = 16;
    
    private Chunk chunk;

    public CacheChunkAction(Chunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public void process(ThreadLink link, DBQueries q) {
        if (!chunk.isLoaded())
            return;
        Set<Block> knownBlocks = new HashSet<Block>();
        try {
            for (BlockState state : q.findAllIn(getBlocks())) {
                Block b = state.getLocation().getBlock();
                knownBlocks.add(b);
                link.setSimpleMetaState(b, state);
            }
            /*int h = chunk.getWorld().getMaxHeight();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < CHUNK_SIZE; x++) {
                    for (int z = 0; z < CHUNK_SIZE; z++) {
                        Block b = chunk.getBlock(x, y, z);
                        if (!knownBlocks.contains(b) && b.getType() != Material.AIR) {
                            link.setSimpleMetaState(b, null);
                            link.blockCount++;
                        }
                    }
                }
            }*/
        } catch (SQLException e) {
            link.getLog().severe(e.getMessage());
            link.getLog().warn("Thread " + Thread.currentThread().getName() + " failed to load BlockStates for Chunk " + chunk.getX() + "/" + chunk.getZ());
        }
    }
    
    protected Cuboid getBlocks() {
        Cuboid c = new Cuboid();
        c.add(chunk.getBlock(0, 0, 0).getLocation());
        
        int h = chunk.getWorld().getMaxHeight();
        c.add(chunk.getBlock(CHUNK_SIZE - 1, h - 1, CHUNK_SIZE - 1).getLocation());
        return c;
    }
}
