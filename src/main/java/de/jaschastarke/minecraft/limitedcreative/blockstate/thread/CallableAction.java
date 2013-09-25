package de.jaschastarke.minecraft.limitedcreative.blockstate.thread;

public abstract class CallableAction<E> implements Action {
    protected boolean returnSet = false;
    protected E returnValue = null;
    
    public E getValue() {
        synchronized (this) {
            try {
                while (!returnSet)
                    this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return returnValue;
    }
}
