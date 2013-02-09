package de.jaschastarke.minecraft.limitedcreative.regions;

import de.jaschastarke.bukkit.lib.configuration.Configuration;
import de.jaschastarke.configuration.IConfigurationNode;
import de.jaschastarke.configuration.IConfigurationSubGroup;
import de.jaschastarke.configuration.InvalidValueException;
import de.jaschastarke.configuration.annotations.IsConfigurationNode;
import de.jaschastarke.minecraft.limitedcreative.ModRegions;
import de.jaschastarke.minecraft.limitedcreative.limits.BlackList;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;

/**
 * Region GameModes-Feature
 * 
 * http://dev.bukkit.org/server-mods/limited-creative/pages/features/region/
 */
public class RegionConfig extends Configuration implements IConfigurationSubGroup {
    protected ModRegions mod;
    protected ModuleEntry<IModule> entry;
    
    public RegionConfig(ModRegions modRegions, ModuleEntry<IModule> modEntry) {
        mod = modRegions;
        entry = modEntry;
    }
    
    @Override
    public void setValue(IConfigurationNode node, Object pValue) throws InvalidValueException {
        if (!(pValue instanceof BlackList))
            super.setValue(node, pValue);
        if (node.getName().equals("enabled")) {
            if (getEnabled()) {
                entry.enable();
            } else {
                entry.disable();
            }
        }
    }

    @Override
    public String getName() {
        return "region";
    }

    @Override
    public int getOrder() {
        return 400;
    }
    
    /**
     * RegionEnabled
     * 
     * Enables the feature for "creative-regions". This Feature is automatically disabled, if the required plugin
     * "WorldGuard" (http://dev.bukkit.org/server-mods/worldguard/) isn't found.
     * 
     * default: true 
     */
    @IsConfigurationNode(order = 100)
    public boolean getEnabled() {
        return config.getBoolean("enabled", true);
    }

    /**
     * RegionMaximumFallingHeight
     * 
     * When the player is more than this count of blocks above the ground, he is prevented from changing the region that
     * sets him survival which would cause him falling and hurting.
     * 
     * default: 3
     */
    @IsConfigurationNode(order = 500)
    public int getMaximumFloatingHeight() {
        return config.getInt("maxFallingHeight", 3);
    }
    
}
