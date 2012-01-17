package de.jaschastarke.minecraft.limitedcreative;

import org.bukkit.configuration.file.FileConfiguration;

public class Configuration {
    public Configuration(FileConfiguration cfg) {
        
    }
    public boolean getStoreCreative() {
        return true;
    }
    public boolean getDropInCreative() {
        return false;
    }
    public String getInventoryFolder() {
        return "inventories";
    }
    public boolean getSignBlock() {
        return true;
    }
}
