package de.jaschastarke.minecraft.lib;

abstract public class Module {
    abstract public String getIdentifier();
    abstract public void init();
    
    public void unload() {
        
    }
}
