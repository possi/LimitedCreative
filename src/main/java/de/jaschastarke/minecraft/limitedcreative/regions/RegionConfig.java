package de.jaschastarke.minecraft.limitedcreative.regions;

import org.bukkit.configuration.ConfigurationSection;

import de.jaschastarke.bukkit.lib.configuration.Configuration;
import de.jaschastarke.bukkit.lib.configuration.ConfigurationContainer;
import de.jaschastarke.configuration.IConfigurationNode;
import de.jaschastarke.configuration.IConfigurationSubGroup;
import de.jaschastarke.configuration.InvalidValueException;
import de.jaschastarke.configuration.annotations.IsConfigurationNode;
import de.jaschastarke.maven.ArchiveDocComments;
import de.jaschastarke.maven.PluginConfigurations;
import de.jaschastarke.minecraft.limitedcreative.Config;
import de.jaschastarke.minecraft.limitedcreative.ModRegions;
import de.jaschastarke.minecraft.limitedcreative.limits.BlackList;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;
import de.jaschastarke.modularize.ModuleEntry.ModuleState;

/**
 * Region GameModes-Feature
 * 
 * http://dev.bukkit.org/server-mods/limited-creative/pages/features/region/
 */
@ArchiveDocComments
@PluginConfigurations(parent = Config.class)
public class RegionConfig extends Configuration implements IConfigurationSubGroup {
    protected ModRegions mod;
    protected ModuleEntry<IModule> entry;
    
    public RegionConfig(ConfigurationContainer container) {
        super(container);
    }
    public RegionConfig(ModRegions modRegions, ModuleEntry<IModule> modEntry) {
        super(modRegions.getPlugin().getDocCommentStorage());
        mod = modRegions;
        entry = modEntry;
    }
    
    @Override
    public void setValue(IConfigurationNode node, Object pValue) throws InvalidValueException {
        if (!(pValue instanceof BlackList))
            super.setValue(node, pValue);
        if (node.getName().equals("enabled")) {
            if (getEnabled()) {
                if (entry.initialState != ModuleState.NOT_INITIALIZED)
                    entry.enable();
            } else {
                entry.disable();
            }
        }
    }
    @Override
    public void setValues(ConfigurationSection sect) {
        super.setValues(sect);
        if (entry.initialState != ModuleState.NOT_INITIALIZED)
            entry.initialState = getEnabled() ? ModuleState.ENABLED : ModuleState.DISABLED;
        
        // Config Upgrade
        if (!sect.contains("rememberOptional") && sect.contains("remember"))
            sect.set("rememberOptional", sect.getBoolean("remember"));
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
     * RegionRememberOptional
     * 
     * Remembers if players disables the Region-GameMode (by switching in an optional region to World-Default-GameMode 
     * with /lc creative|survival). So when the player re-enters the region, he keep his GameMode which he left it in.
     * Hint: This is very confusing, if MultiVerse "enforce gamemode" swaps your state (default). So better don't use 
     * with Multiverse.
     * 
     * default: false
     */
    @IsConfigurationNode(order = 200)
    public boolean getRememberOptional() {
        return config.getBoolean("rememberOptional", false);
    }

    /**
     * RegionSafeMode
     * 
     * When a player leaves a region he always will get back to the World-GameMode, even if he entered the region already 
     * in the Region-GameMode. So its the opposite analog to RegionRememberOptional.
     * That means: If a GM in creative-mode walks/flies through a creative-region in a survival world, he will get back 
     * to survival on leaving the region.
     * 
     * default: false
     */
    @IsConfigurationNode(order = 300)
    public boolean getSafeMode() {
        return config.getBoolean("safemode", false);
    }
    

    /**
     * RegionMaximumFallingHeight
     * 
     * When the player is more than this count of blocks above the ground, he is prevented from changing the region that 
     * sets him survival which would cause him falling and hurting.
     * 
     * Set to -1 to disable
     * 
     * default: 3
     */
    @IsConfigurationNode(order = 500)
    public int getMaxFallingHeight() {
        return config.getInt("maxFallingHeight", 3);
    }
    
}
