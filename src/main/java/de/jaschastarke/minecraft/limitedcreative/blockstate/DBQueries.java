package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.GameMode;
import org.bukkit.Location;

import de.jaschastarke.database.Type;
import de.jaschastarke.database.db.Database;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockState.Source;

public class DBQueries {
    private Database db;
    public DBQueries(Database db) {
        this.db = db;
    }
    
    private PreparedStatement find = null;
    public BlockState find(Location loc) throws SQLException {
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
            bs.setDate(rs.getDate("cdate"));
            bs.setGameMode(getGameMode(rs));
            bs.setPlayerName(rs.getString("player"));
            bs.setSource(getSource(rs));
            return bs;
        }
        return null;
    }

    private PreparedStatement delete = null;
    public boolean delete(BlockState s) throws SQLException {
        if (delete == null) {
            delete = db.prepare("DELETE FROM lc_block_state WHERE x = ? AND y = ? AND z = ? AND world = ?");
        }
        delete.setInt(1, s.getLocation().getBlockX());
        delete.setInt(2, s.getLocation().getBlockY());
        delete.setInt(3, s.getLocation().getBlockZ());
        delete.setString(4, s.getLocation().getWorld().getUID().toString());
        return delete.executeUpdate() > 0;
    }
    
    private PreparedStatement insert = null;
    public boolean insert(BlockState s) throws SQLException {
        if (insert == null) {
            insert = db.prepare("INSERT INTO lc_block_state (x, y, z, world, gm, player, cdate, source)"+
                            " VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        }
        insert.setInt(1, s.getLocation().getBlockX());
        insert.setInt(2, s.getLocation().getBlockY());
        insert.setInt(3, s.getLocation().getBlockZ());
        insert.setString(4, s.getLocation().getWorld().getUID().toString());
        if (db.getType() == Type.MySQL)
            insert.setString(5, s.getGameMode().name());
        else
            insert.setInt(5, s.getGameMode().getValue());
        insert.setString(6, s.getPlayerName());
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
                    return GameMode.getByValue(rs.getInt("gm"));
                case MySQL:
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
                            "constraint ck_lc_block_state_source check (source in (0,1,2,3))"+
                        ")"
                    );
                    db.getLogger().info("Created SQLite-Table: lc_block_state");
                }
                break;
            case MySQL:
                if (!db.getDDL().tableExists("lc_block_state")) {
                    db.execute(
                        "CREATE TABLE IF NOT EXISTS lc_block_state ("+
                            "x                         INT NOT NULL,"+
                            "y                         INT NOT NULL,"+
                            "z                         INT NOT NULL,"+
                            "world                     VARCHAR(40) NOT NULL,"+
                            "gm                        ENUM('CREATIVE', 'SURVIVAL', 'ADVENTURE'),"+
                            "player                    VARCHAR(255),"+
                            "cdate                     TIMESTAMP NOT NULL,"+
                            "source                    ENUM('SEED','PLAYER','EDIT','UNKNOWN'),"+
                            "PRIMARY KEY (x, y, z, world)"+
                        ")"
                    );
                    db.getLogger().info("Created MySQL-Table: lc_block_state");
                }
                break;
            default:
                throw new RuntimeException("Currently only SQLite is supported.");
        }
    }
}
