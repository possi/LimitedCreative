package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.GameMode;
import org.bukkit.Location;

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
            find = db.prepare("SELECT * FROM block_state WHERE x = ? AND y = ? AND z = ? AND world = ?");
        }
        find.setInt(1, loc.getBlockX());
        find.setInt(2, loc.getBlockX());
        find.setInt(3, loc.getBlockX());
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
            delete = db.prepare("DELETE FROM block_state WHERE x = ? AND y = ? AND z = ? AND world = ?");
        }
        delete.setInt(1, s.getLocation().getBlockX());
        delete.setInt(2, s.getLocation().getBlockX());
        delete.setInt(3, s.getLocation().getBlockX());
        delete.setString(4, s.getLocation().getWorld().getUID().toString());
        return delete.executeUpdate() > 0;
    }
    
    private GameMode getGameMode(ResultSet rs) {
        switch (db.getType()) {
            case SQLite:
                try {
                    return GameMode.values()[rs.getInt("gm")];
                } catch (Exception e) {
                    db.getLogger().warn("Couldn't get GameMode from result-set: "+e.getMessage());
                    return GameMode.SURVIVAL;
                }
            default:
                throw new RuntimeException("Currently only SQLite is supported.");
        }
    }
    
    private Source getSource(ResultSet rs) {
        switch (db.getType()) {
            case SQLite:
                try {
                    return Source.values()[rs.getInt("source")];
                } catch (Exception e) {
                    db.getLogger().warn("Couldn't get Source from result-set: "+e.getMessage());
                    return Source.UNKNOWN;
                }
            default:
                throw new RuntimeException("Currently only SQLite is supported.");
        }
    }
    
    public void initTable() throws SQLException {
        switch (db.getType()) {
            case SQLite:
                if (db.getDDL().tableExists("block_state")) {
                    db.execute(
                        "CREATE TABLE block_state ("+
                            "x                         integer,"+
                            "y                         integer,"+
                            "z                         integer,"+
                            "world                     varchar(40),"+
                            "gm                        integer,"+
                            "player                    varchar(255),"+
                            "cdate                     timestamp not null,"+
                            "source                    integer not null,"+
                            "constraint ck_block_state_gm check (gm in (0,1,2)),"+
                            "constraint ck_block_state_source check (source in (0,1,2,3))"+
                        ")"
                    );
                    db.getLogger().info("Created SQLite-Table: block_state");
                }
                break;
            default:
                throw new RuntimeException("Currently only SQLite is supported.");
        }
    }
}
