package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;

import de.jaschastarke.bukkit.lib.database.ResultIterator;
import de.jaschastarke.database.Type;
import de.jaschastarke.database.db.Database;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockState.Source;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBModel.Cleanup;
import de.jaschastarke.utils.IDebugLogHolder;

public class DBQueries {
    private Database db;
    private IDebugLogHolder dbg;
    public DBQueries(IDebugLogHolder mod, Database db) {
        this.dbg = mod;
        this.db = db;
    }
    
    private PreparedStatement find = null;
    public BlockState find(Location loc) throws SQLException {
        if (dbg.isDebug())
            dbg.getLog().debug("DBQuery: find: " + loc.toString());
        if (find == null) {
            find = db.prepare("SELECT * FROM lc_block_state WHERE x = ? AND y = ? AND z = ? AND world = ?");
        }
        find.setInt(1, loc.getBlockX());
        find.setInt(2, loc.getBlockY());
        find.setInt(3, loc.getBlockZ());
        find.setString(4, loc.getWorld().getUID().toString());
        ResultSet rs = find.executeQuery();
        if (rs.next()) {
            BlockState bs = new BlockState();
            bs.setLocation(loc);
            bs.setDate(rs.getTimestamp("cdate"));
            bs.setGameMode(getGameMode(rs));
            bs.setPlayerName(rs.getString("player"));
            bs.setSource(getSource(rs));
            rs.close();
            return bs;
        }
        rs.close();
        return null;
    }
    
    private PreparedStatement findall = null;
    public List<BlockState> findAllIn(DBModel.Cuboid c) throws SQLException {
        if (dbg.isDebug())
            dbg.getLog().debug("DBQuery: findAllIn: " + c.toString());
        List<BlockState> blocks = new ArrayList<BlockState>();
        if (findall == null) {
            findall = db.prepare("SELECT * FROM lc_block_state WHERE x >= ? AND x <= ? AND y >= ? AND y <= ? AND z >= ? AND z <= ? AND world = ?");
        }
        findall.setInt(1, c.getMinX());
        findall.setInt(2, c.getMaxX());
        findall.setInt(3, c.getMinY());
        findall.setInt(4, c.getMaxY());
        findall.setInt(5, c.getMinZ());
        findall.setInt(6, c.getMaxZ());
        findall.setString(7, c.getWorld().getUID().toString());
        ResultSet rs = findall.executeQuery();
        while (rs.next()) {
            BlockState bs = new BlockState();
            bs.setLocation(new Location(c.getWorld(), rs.getInt("x"), rs.getInt("y"), rs.getInt("z")));
            bs.setDate(rs.getTimestamp("cdate"));
            bs.setGameMode(getGameMode(rs));
            bs.setPlayerName(rs.getString("player"));
            bs.setSource(getSource(rs));
            blocks.add(bs);
        }
        rs.close();
        return blocks;
    }
    public Iterable<BlockState> iterateAllIn(final DBModel.Cuboid c) throws SQLException {
        if (dbg.isDebug())
            dbg.getLog().debug("DBQuery: iterateAllIn: " + c.toString());
        if (findall == null) {
            findall = db.prepare("SELECT * FROM lc_block_state WHERE x >= ? AND x <= ? AND y >= ? AND y <= ? AND z >= ? AND z <= ? AND world = ?");
        }
        findall.setInt(1, c.getMinX());
        findall.setInt(2, c.getMaxX());
        findall.setInt(3, c.getMinY());
        findall.setInt(4, c.getMaxY());
        findall.setInt(5, c.getMinZ());
        findall.setInt(6, c.getMaxZ());
        findall.setString(7, c.getWorld().getUID().toString());
        ResultSet rs = findall.executeQuery();
        return new ResultIterator<BlockState>(rs) {
            @Override
            protected BlockState fetch(ResultSet rs) throws SQLException {
                BlockState bs = new BlockState();
                bs.setLocation(new Location(c.getWorld(), rs.getInt("x"), rs.getInt("y"), rs.getInt("z")));
                bs.setDate(rs.getTimestamp("cdate"));
                bs.setGameMode(getGameMode(rs));
                bs.setPlayerName(rs.getString("player"));
                bs.setSource(getSource(rs));
                return bs;
            }
        };
    }

    private PreparedStatement delete = null;
    public boolean delete(BlockState s) throws SQLException {
        return delete(s.getLocation());
    }
    public boolean delete(Location loc) throws SQLException {
        if (dbg.isDebug())
            dbg.getLog().debug("DBQuery: delete: " + loc.toString());
        if (delete == null) {
            delete = db.prepare("DELETE FROM lc_block_state WHERE x = ? AND y = ? AND z = ? AND world = ?");
        }
        delete.setInt(1, loc.getBlockX());
        delete.setInt(2, loc.getBlockY());
        delete.setInt(3, loc.getBlockZ());
        delete.setString(4, loc.getWorld().getUID().toString());
        return delete.executeUpdate() > 0;
    }

    private PreparedStatement update = null;
    public boolean update(BlockState s) throws SQLException {
        if (dbg.isDebug())
            dbg.getLog().debug("DBQuery: update: " + s.toString());
        if (update == null) {
            update = db.prepare("UPDATE lc_block_state SET gm = ?, player = ?, cdate = ?, source = ?"+
                        "WHERE x = ? AND y = ? AND z = ? AND world = ? ");
        }
        if (s.getGameMode() == null)
            update.setNull(1, Types.INTEGER);
        else if (db.getType() == Type.MySQL)
            update.setString(1, s.getGameMode().name());
        else
            update.setInt(1, s.getGameMode().getValue());
        update.setString(2, s.getPlayerUUID().toString());
        update.setTimestamp(3, new java.sql.Timestamp(s.getDate().getTime()));
        if (db.getType() == Type.MySQL)
            update.setString(4, s.getSource().name());
        else
            update.setInt(4, s.getSource().ordinal());
        update.setInt(5, s.getLocation().getBlockX());
        update.setInt(6, s.getLocation().getBlockY());
        update.setInt(7, s.getLocation().getBlockZ());
        update.setString(8, s.getLocation().getWorld().getUID().toString());
        return update.executeUpdate() > 0;
    }

    private PreparedStatement move = null;
    public boolean move(BlockState s, Location newLoc) throws SQLException {
        boolean r = move(s.getLocation(), newLoc);
        if (r)
            s.setLocation(newLoc);
        return r;
    }

    public boolean move(Location oldLoc, Location newLoc) throws SQLException {
        if (dbg.isDebug())
            dbg.getLog().debug("DBQuery: move: " + oldLoc.toString() + ", " + newLoc.toString());
        if (move == null) {
            move = db.prepare("UPDATE lc_block_state SET x = ?, y = ?, z = ? "+
                        "WHERE x = ? AND y = ? AND z = ? AND world = ?");
        }
        
        move.setInt(1, newLoc.getBlockX());
        move.setInt(2, newLoc.getBlockY());
        move.setInt(3, newLoc.getBlockZ());
        move.setInt(4, oldLoc.getBlockX());
        move.setInt(5, oldLoc.getBlockY());
        move.setInt(6, oldLoc.getBlockZ());
        move.setString(7, oldLoc.getWorld().getUID().toString());
        
        return move.executeUpdate() > 0;
    }

    /*private PreparedStatement replace = null;
    public boolean replace(BlockState s) throws SQLException {
        if (replace == null) {
            replace = db.prepare("INSERT OR REPLACE INTO lc_block_state (x, y, z, world, gm, player, cdate, source)"+
                            " VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        }
        return this._executeInsert(replace, s);
    }*/
    private PreparedStatement insert = null;
    public boolean insert(BlockState s) throws SQLException {
        if (dbg.isDebug())
            dbg.getLog().debug("DBQuery: insert: " + s.toString());
        if (insert == null) {
            insert = db.prepare("INSERT INTO lc_block_state (x, y, z, world, gm, player, cdate, source)"+
                            " VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        }
        return this._executeInsert(insert, s);
    }
    private boolean _executeInsert(PreparedStatement insert, BlockState s) throws SQLException {
        insert.setInt(1, s.getLocation().getBlockX());
        insert.setInt(2, s.getLocation().getBlockY());
        insert.setInt(3, s.getLocation().getBlockZ());
        insert.setString(4, s.getLocation().getWorld().getUID().toString());
        if (s.getGameMode() == null)
            insert.setNull(5, Types.INTEGER);
        else if (db.getType() == Type.MySQL)
            insert.setString(5, s.getGameMode().name());
        else
            insert.setInt(5, s.getGameMode().getValue());
        insert.setString(6, s.getPlayerUUID().toString());
        insert.setTimestamp(7, new java.sql.Timestamp(s.getDate().getTime()));
        if (db.getType() == Type.MySQL)
            insert.setString(8, s.getSource().name());
        else
            insert.setInt(8, s.getSource().ordinal());
        return insert.executeUpdate() > 0;
    }
    
    private GameMode getGameMode(ResultSet rs) {
        try {
            switch (db.getType()) {
                case SQLite:
                    int gm = rs.getInt("gm");
                    if (rs.wasNull())
                        return null;
                    return GameMode.getByValue(gm);
                case MySQL:
                    if (rs.getString("gm") == null)
                        return null;
                    return GameMode.valueOf(rs.getString("gm"));
                default:
                    throw new RuntimeException("Unsupported Database-Type.");
            }
        } catch (SQLException e) {
            db.getLogger().warn("Couldn't get GameMode from result-set: "+e.getMessage());
            return GameMode.SURVIVAL;
        }
    }
    
    private Source getSource(ResultSet rs) {
        try {
            switch (db.getType()) {
                case SQLite:
                    return Source.values()[rs.getInt("source")];
                case MySQL:
                    return Source.valueOf(rs.getString("source"));
                default:
                    throw new RuntimeException("Unsupported Database-Type.");
            }
        } catch (Exception e) {
            db.getLogger().warn("Couldn't get Source from result-set: "+e.getMessage());
            return Source.UNKNOWN;
        }
    }
    
    public void initTable() throws SQLException {
        switch (db.getType()) {
            case SQLite:
                if (!db.getDDL().tableExists("lc_block_state")) {
                    if (dbg.isDebug())
                        dbg.getLog().debug("DBQuery: initTable SQLite: lc_block_state");
                    db.execute(
                        "CREATE TABLE lc_block_state ("+
                            "x                         integer,"+
                            "y                         integer,"+
                            "z                         integer,"+
                            "world                     varchar(40),"+
                            "gm                        integer,"+
                            "player                    varchar(255),"+
                            "cdate                     timestamp not null,"+
                            "source                    integer not null,"+
                            "primary key (x, y, z, world),"+
                            "constraint ck_lc_block_state_gm check (gm in (0,1,2)),"+
                            "constraint ck_lc_block_state_source check (source in (0,1,2,3,4))"+
                        ")"
                    ).close();
                    db.getLogger().info("Created SQLite-Table: lc_block_state");
                }
                break;
            case MySQL:
                if (!db.getDDL().tableExists("lc_block_state")) {
                    if (dbg.isDebug())
                        dbg.getLog().debug("DBQuery: initTable MySQL: lc_block_state");
                    db.execute(
                        "CREATE TABLE IF NOT EXISTS lc_block_state ("+
                            "x                         INT NOT NULL,"+
                            "y                         INT NOT NULL,"+
                            "z                         INT NOT NULL,"+
                            "world                     VARCHAR(40) NOT NULL,"+
                            "gm                        ENUM('CREATIVE', 'SURVIVAL', 'ADVENTURE'),"+
                            "player                    VARCHAR(255),"+
                            "cdate                     TIMESTAMP NOT NULL,"+
                            "source                    ENUM('SEED','PLAYER','EDIT','COMMAND','UNKNOWN') NOT NULL,"+
                            "PRIMARY KEY (x, y, z, world)"+
                        ")"
                    ).close();
                    db.getLogger().info("Created MySQL-Table: lc_block_state");
                }
                break;
            default:
                throw new RuntimeException("Currently only SQLite or MySQL is supported.");
        }
    }

    public int cleanup(Cleanup q) throws SQLException {
        PreparedStatement delete;
        if (dbg.isDebug())
            dbg.getLog().debug("DBQuery: cleanup: " + q.toString());
        if (q == Cleanup.SURVIVAL) {
            delete = db.prepare("DELETE FROM lc_block_state WHERE gm = ?");
            if (db.getType() == Type.MySQL)
                delete.setString(1, GameMode.SURVIVAL.name());
            else
                delete.setInt(1, GameMode.SURVIVAL.getValue());
        } else {
            return -1;
        }
        return delete.executeUpdate();
    }
    
    public Database getDB() {
        return db;
    }
}
