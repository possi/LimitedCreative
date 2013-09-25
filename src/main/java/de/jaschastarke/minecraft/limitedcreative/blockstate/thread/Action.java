package de.jaschastarke.minecraft.limitedcreative.blockstate.thread;

import de.jaschastarke.minecraft.limitedcreative.blockstate.DBQueries;

public interface Action {

    /**
     * @internal Executed from asynchronous Thread. Only Thread-Safe methods should be called.
     */
    void process(ThreadLink link, DBQueries q);
}
