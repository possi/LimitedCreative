package de.jaschastarke.minecraft.limitedcreative.blockstate.thread;

import java.sql.SQLException;

import org.bukkit.block.Block;

import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockState;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBQueries;

public class FetchBlockStateAction extends CallableAction<BlockState> {
    private Block block;

    public FetchBlockStateAction(Block block) {
        this.block = block;
    }

    @Override
    public void process(ThreadLink link, DBQueries q) {
        BlockState state = null;
        try {
            state = q.find(block.getLocation());
            link.setMetaState(block, state);
        } catch (SQLException e) {
            link.getLog().severe(e.getMessage());
            link.getLog().warn("Thread " + Thread.currentThread().getName() + " failed to fetch BlockState from DB: " + block.getLocation());
            return;
        }
        synchronized (this) {
            returnValue = state;
            returnSet = true;
            this.notify();
        }
    }
}
