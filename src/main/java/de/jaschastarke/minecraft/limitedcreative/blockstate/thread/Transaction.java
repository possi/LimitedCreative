package de.jaschastarke.minecraft.limitedcreative.blockstate.thread;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.block.Block;

import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockState;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBModel.DBTransaction;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBQueries;

abstract public class Transaction implements DBTransaction, Action {
    protected boolean finished = false;
    private List<TransactionAction> actions = new LinkedList<TransactionAction>();
    private ThreadLink link;
    
    public Transaction(ThreadLink link) {
        this.link = link;
    }
    
    @Override
    public void moveState(Block from, Block to) {
        if (finished)
            throw new IllegalAccessError("Transaction already ended");
        
        actions.add(new MoveBlockStateAction(from.getLocation(), to.getLocation()));
    }

    @Override
    public void setState(BlockState state) {
        if (finished)
            throw new IllegalAccessError("Transaction already ended");
        
        Block block = state.getLocation().getBlock();
        actions.add(new UpdateBlockStateAction(block));
    }

    public void setState(Block block) {
        if (finished)
            throw new IllegalAccessError("Transaction already ended");
        
        actions.add(new UpdateBlockStateAction(block));
    }
    
    @Override
    public void finish() {
        if (finished)
            return;
        link.queueTransaction(this);
    }
    
    /**
     * @internal Executed from asynchronous Thread. Only Thread-Safe methods should be called.
     */
    @Override
    public void process(ThreadLink link, DBQueries q) {
        if (actions.isEmpty())
            return;
        try {
            q.getDB().startTransaction();
            for (TransactionAction act : actions) {
                act.processInTransaction(link, q);
            }
            q.getDB().endTransaction();
        } catch (SQLException e) {
            try {
                q.getDB().revertTransaction();
            } catch (SQLException e1) {}
            link.getLog().severe(e.getMessage());
            link.getLog().warn("Thread " + Thread.currentThread().getName() + " failed to write Transaction to the Database");
        }
    }
}