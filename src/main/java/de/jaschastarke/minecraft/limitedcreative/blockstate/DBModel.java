package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public interface DBModel {
    public static class Cuboid {
        private World w = null;
        private int minx, miny, minz;
        private int maxx, maxy, maxz;
        public void add(Location loc) {
            if (w == null) {
                w = loc.getWorld();
                minx = maxx = loc.getBlockX();
                miny = maxy = loc.getBlockY();
                minz = maxz = loc.getBlockZ();
            } else {
                if (w != loc.getWorld())
                    throw new IllegalArgumentException("Point is from a different world");
                if (minx > loc.getBlockX())
                    minx = loc.getBlockX();
                if (maxx < loc.getBlockX())
                    maxx = loc.getBlockX();
                if (miny > loc.getBlockY())
                    miny = loc.getBlockY();
                if (maxy < loc.getBlockY())
                    maxy = loc.getBlockY();
                if (minz > loc.getBlockZ())
                    minz = loc.getBlockZ();
                if (maxz < loc.getBlockZ())
                    maxz = loc.getBlockZ();
            }
        }
        public int getMinX() {
            return minx;
        }
        public int getMinY() {
            return miny;
        }
        public int getMinZ() {
            return minz;
        }
        public int getMaxX() {
            return maxx;
        }
        public int getMaxY() {
            return maxy;
        }
        public int getMaxZ() {
            return maxz;
        }
        public World getWorld() {
            return w;
        }
        public boolean isEmpty() {
            return w == null;
        }
        public String toString() {
            return "Cuboid{world="+w.getName()+", min_x="+minx+", max_x="+maxx+", min_y="+miny+", max_y="+maxy+", min_z="+minz+", max_z="+maxz+"}";
        }
    }
    
    public enum Cleanup {
        SURVIVAL;
    }
    
    public void onEnable() throws Exception;
    public void onDisable();
    public void moveState(Block from, Block to);
    public void removeState(BlockState state);
    public void removeState(Block block);
    public Map<Block, BlockState> getStates(List<Block> blocks);
    public Map<Block, Boolean> getRestrictedStates(List<Block> blocks);
    public void cacheStates(DBModel.Cuboid c);
    public BlockState getState(Block block);
    public boolean isRestricted(Block block);
    public void setState(BlockState state);
    public DBTransaction groupUpdate();
    
    public static interface DBTransaction {
        public void moveState(Block from, Block to);
        public void setState(BlockState state);
        public void removeState(Block block);
        public void finish();
    }

    public int cleanUp(Cleanup target);
}
