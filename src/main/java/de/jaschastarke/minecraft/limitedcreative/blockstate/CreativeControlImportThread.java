package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;

import de.jaschastarke.bukkit.lib.chat.ChatFormattings;
import de.jaschastarke.bukkit.lib.commands.CommandContext;
import de.jaschastarke.bukkit.lib.database.ResultIterator;
import de.jaschastarke.database.db.Database;
import de.jaschastarke.minecraft.limitedcreative.ModBlockStates;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockState.Source;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBModel.Cuboid;

public class CreativeControlImportThread extends DatabaseMigrationThread {
    public CreativeControlImportThread(ModBlockStates mod, CommandContext context, Database source, Database target) {
        super(mod, context, source, target);
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
            
            DBQueries targetDB = new DBQueries(this, target);
            
            List<WorldSize> worldBounds = new ArrayList<WorldSize>();
            for (World w : mod.getPlugin().getServer().getWorlds()) {
                try {
                    ResultSet fetchBounds = sourceConnection.createStatement().executeQuery("SELECT MIN(x), MIN(z), MAX(x), MAX(z) FROM crcr_blocks_" + w.getName());
                    while (fetchBounds.next()) {
                        worldBounds.add(new WorldSize(w,
                                        fetchBounds.getInt(1),
                                        fetchBounds.getInt(2),
                                        fetchBounds.getInt(3),
                                        fetchBounds.getInt(4)));
                    }
                    fetchBounds.close();
                } catch (SQLException e) {
                    if (isDebug())
                        mod.getLog().debug("crcr_blocks_" + w.getName() + " not found: " + e.getMessage());
                    mod.getLog().info("CreativeControl has BlockData for World " + w.getName());
                }
            }
            
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
                            
                            for (BlockState bs : iterateAllIn(c)) {
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

    private PreparedStatement findall = null;
    private Iterable<BlockState> iterateAllIn(final DBModel.Cuboid c) throws SQLException {
        if (isDebug())
            getLog().debug("DBQuery: iterateAllIn: " + c.toString());
        if (findall == null) {
            findall = source.prepare("SELECT * FROM crcr_blocks_" + c.getWorld().getName() + " LEFT JOIN crcr_players ON owner = id WHERE x >= ? AND x <= ? AND y >= ? AND y <= ? AND z >= ? AND z <= ?");
        }
        findall.setInt(1, c.getMinX());
        findall.setInt(2, c.getMaxX());
        findall.setInt(3, c.getMinY());
        findall.setInt(4, c.getMaxY());
        findall.setInt(5, c.getMinZ());
        findall.setInt(6, c.getMaxZ());
        ResultSet rs = findall.executeQuery();
        return new ResultIterator<BlockState>(rs) {
            @Override
            protected BlockState fetch(ResultSet rs) throws SQLException {
                BlockState bs = new BlockState();
                bs.setLocation(new Location(c.getWorld(), rs.getInt("x"), rs.getInt("y"), rs.getInt("z")));
                bs.setDate(new Date(rs.getLong("time")));
                bs.setGameMode(GameMode.CREATIVE);
                bs.setPlayerName(rs.getString("player"));
                bs.setSource(Source.PLAYER);
                return bs;
            }
        };
    }
}
