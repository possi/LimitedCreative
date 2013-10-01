package de.jaschastarke.minecraft.limitedcreative.blockstate.thread;

import java.sql.SQLException;

import org.bukkit.block.Block;

import de.jaschastarke.minecraft.limitedcreative.blockstate.AbstractModel.HasBlockState;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBQueries;

public class UpdateBlockStateAction extends TransactionAction implements Action {
    private Block block;
    public UpdateBlockStateAction(Block block) {
        this.block = block;
    }

    @Override
    public void process(ThreadLink link, DBQueries q) {
        HasBlockState state = link.getMetaState(block);
        if (state.isSet()) {
            try {
                q.delete(block.getLocation());
                if (state.getState() != null)
                    q.insert(state.getState());
            } catch (SQLException e) {
                link.getLog().severe(e.getMessage());
                link.getLog().warn("Thread " + Thread.currentThread().getName() + " failed to save BlockState to DB: " + state.getState());
            }
        }
    }

    @Override
    public void processInTransaction(ThreadLink link, DBQueries q) throws SQLException {
        HasBlockState state = link.getMetaState(block);
        if (state.isSet()) {
            q.delete(block.getLocation());
            if (state.getState() != null)
                q.insert(state.getState());
        }
    }

}