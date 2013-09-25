package de.jaschastarke.minecraft.limitedcreative.blockstate.thread;

import java.sql.SQLException;

import de.jaschastarke.minecraft.limitedcreative.blockstate.DBQueries;

abstract public class TransactionAction implements Action {
    abstract public void processInTransaction(ThreadLink link, DBQueries q) throws SQLException;
}
