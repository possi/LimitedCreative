package de.jaschastarke.minecraft.limitedcreative.blockstate.thread;

import java.sql.SQLException;
import java.util.List;

import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockState;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBModel.Cuboid;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBQueries;

public class FetchCuboidAction extends CallableAction<List<BlockState>> {
    private Cuboid cuboid;

    public FetchCuboidAction(Cuboid cuboid) {
        this.cuboid = cuboid;
    }

    @Override
    public void process(ThreadLink link, DBQueries q) {
        List<BlockState> states = null;
        try {
            states = q.findAllIn(cuboid);
            for (BlockState bs : states) {
                link.setMetaState(bs.getLocation().getBlock(), bs);
            }
        } catch (SQLException e) {
            link.getLog().severe(e.getMessage());
            link.getLog().warn("Thread " + Thread.currentThread().getName() + " failed to fetch BlockState from DB: " + cuboid);
            return;
        }
        synchronized (this) {
            returnValue = states;
            returnSet = true;
            this.notify();
        }
    }
}
