package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import de.jaschastarke.bukkit.lib.chat.ChatFormattings;
import de.jaschastarke.bukkit.lib.commands.CommandContext;
import de.jaschastarke.database.db.Database;
import de.jaschastarke.minecraft.limitedcreative.ModBlockStates;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBModel.Cuboid;
import de.jaschastarke.utils.IDebugLogHolder;
import de.jaschastarke.utils.ISimpleLogger;

public class DatabaseMigrationThread extends Thread implements IDebugLogHolder {
    private static final int CHUNK_SIZE = 512;
    private ModBlockStates mod;
    private CommandContext context;
    private Database source;
    private Database target;
    private Mode mode = Mode.REPLACE;
    private boolean debug = false;
    
    public static enum Mode {
        REPLACE,
        UPDATE
    }

    public DatabaseMigrationThread(ModBlockStates mod, CommandContext context, Database source, Database target) {
        this.mod = mod;
        this.context = context;
        this.source = source;
        this.target = target;
        setName("LC BlockState Database-Migration");
        setPriority(MIN_PRIORITY);
    }
    
    public Mode getMode() {
        return mode;
    }
    public void setMode(Mode mode) {
        this.mode = mode;
    }
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    @Override
    public ISimpleLogger getLog() {
        return mod.getLog();
    }

    @Override
    public void run() {
        try {
            if (!target.isInTransaction())
                target.startTransaction();
            
            int rowCount = 0;
            Connection sourceConnection = source.getConnection();
            Connection targetConnection = target.getConnection();
            
            if (mode == Mode.REPLACE) {
                targetConnection.createStatement().execute("DELETE FROM lc_block_state");
            }
            
            DBQueries sourceDB = new DBQueries(this, source);
            DBQueries targetDB = new DBQueries(this, target);
            
            List<WorldSize> worldBounds = new ArrayList<WorldSize>();
            ResultSet fetchBounds = sourceConnection.createStatement().executeQuery("SELECT world, MIN(x), MIN(z), MAX(x), MAX(z) FROM lc_block_state GROUP BY world");
            while (fetchBounds.next()) {
                worldBounds.add(new WorldSize(fetchBounds.getString("world"),
                                fetchBounds.getInt(2),
                                fetchBounds.getInt(3),
                                fetchBounds.getInt(4),
                                fetchBounds.getInt(5)));
            }
            fetchBounds.close();
            
            for (WorldSize bounds : worldBounds) {
                World world = mod.getPlugin().getServer().getWorld(bounds.getWorld());
                if (world != null) {
                    long time = System.currentTimeMillis();
                    int itCount = 0;
                    if (mod.isDebug())
                        mod.getLog().debug("Processing world " + world.getName() + " with bounds: " + bounds);
                    
                    for (int x = bounds.getMinX(); x <= bounds.getMaxX(); x += CHUNK_SIZE + 1) {
                        for (int z = bounds.getMinZ(); z <= bounds.getMaxZ(); z += CHUNK_SIZE + 1) {
                            Cuboid c = new Cuboid();
                            c.add(new Location(world, x, 0, z));
                            c.add(new Location(world, x + CHUNK_SIZE, world.getMaxHeight(), z + CHUNK_SIZE));
                            System.out.println("Fetching Cuboid: " + c.toString());
                            
                            for (BlockState bs : sourceDB.iterateAllIn(c)) {
                                if (mode == Mode.UPDATE) {
                                    BlockState xs = targetDB.find(bs.getLocation());
                                    if (xs == null) {
                                        targetDB.insert(bs);
                                    } else if (xs.getDate().before(bs.getDate())) {
                                        targetDB.update(bs);
                                    }
                                } else {
                                    targetDB.insert(bs);
                                }
                                rowCount++;
                                itCount++;
                            }
                            
                            Thread.yield();
                        }
                    }
                    String region = "Region{world = " + world.getName() + ", x = [" + bounds.getMinX() + "; " + (bounds.getMinX() + CHUNK_SIZE) + "], z = [" + bounds.getMinZ() + "; " + (bounds.getMinZ() + CHUNK_SIZE) + "]}";
                    mod.getLog().info("Migration processed " + itCount + " BlockStates in " + region + " within " + ((System.currentTimeMillis() - time) / 1000.0) + " seconds");
                }
            }
            
            target.endTransaction();
            context.responseFormatted(ChatFormattings.SUCCESS, L("command.blockstate.migration_finished", rowCount) + " " +
                        context.getFormatter().formatString(ChatFormattings.ERROR, L("command.blockstate.migration_finished_restart")));
        } catch (SQLException e) {
            try {
                target.revertTransaction();
            } catch (SQLException e1) {}
            context.responseFormatted(ChatFormattings.ERROR, L("command.blockstate.migration_error", e.getMessage()));
        }
    }
    
    private String L(String msg, Object... args) {
        return mod.getPlugin().getLocale().trans(msg, args);
    }
    
    private static class WorldSize {
        UUID w;
        int minX, minZ, maxX, maxZ;
        
        public WorldSize(String w, int minX, int minZ, int maxX, int maxZ) {
            this.w = UUID.fromString(w);
            this.minX = minX;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxZ = maxZ;
        }
        public String toString() {
            World world = Bukkit.getServer().getWorld(w);
            String wn = world == null ? w.toString() : world.getName();
            return getClass().getSimpleName() + "{world = " + wn + ", minX = " + minX + ", minZ = " + minZ + ", maxX = " + maxX + ", maxZ = " + maxZ + "}";
        }
        public UUID getWorld() {
            return w;
        }
        public int getMinX() {
            return minX;
        }
        public int getMinZ() {
            return minZ;
        }
        public int getMaxX() {
            return maxX;
        }
        public int getMaxZ() {
            return maxZ;
        }
    }
}
