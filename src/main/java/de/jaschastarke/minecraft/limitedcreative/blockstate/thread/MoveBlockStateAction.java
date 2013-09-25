package de.jaschastarke.minecraft.limitedcreative.blockstate.thread;

import java.sql.SQLException;

import org.bukkit.Location;

import de.jaschastarke.minecraft.limitedcreative.blockstate.DBQueries;

public class MoveBlockStateAction extends TransactionAction implements Action {
    private Location from;
    private Location to;

    public MoveBlockStateAction(Location from, Location to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public void process(ThreadLink link, DBQueries q) {
        try {
            processInTransaction(link, q);
        } catch (SQLException e) {
            link.getLog().severe(e.getMessage());
            link.getLog().warn("Thread " + Thread.currentThread().getName() + " failed to move BlockState in DB from " + from.toString() + " to " + to.toString());
        }
    }

    @Override
    public void processInTransaction(ThreadLink link, DBQueries q) throws SQLException {
        q.delete(to);
        q.move(from, to);
    }

}