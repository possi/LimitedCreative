package de.jaschastarke.minecraft.limitedcreative.blockstate;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import de.jaschastarke.bukkit.lib.configuration.Configuration;
import de.jaschastarke.configuration.IConfigurationNode;
import de.jaschastarke.configuration.IConfigurationSubGroup;
import de.jaschastarke.configuration.InvalidValueException;
import de.jaschastarke.configuration.annotations.IsConfigurationNode;
import de.jaschastarke.maven.ArchiveDocComments;
import de.jaschastarke.minecraft.limitedcreative.ModBlockStates;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;
import de.jaschastarke.modularize.ModuleEntry.ModuleState;

/**
 * BlockState-Feature
 * 
 * http://dev.bukkit.org/server-mods/limited-creative/pages/features/blockstate/
 */
@ArchiveDocComments
public class BlockStateConfig extends Configuration implements IConfigurationSubGroup {
    protected ModBlockStates mod;
    protected ModuleEntry<IModule> entry;
    
    public BlockStateConfig(ModBlockStates mod, ModuleEntry<IModule> modEntry) {
        this.mod = mod;
        entry = modEntry;
    }
    
    @Override
    public void setValue(IConfigurationNode node, Object pValue) throws InvalidValueException {
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
    }
    @Override
    public String getName() {
        return "blockstate";
    }
    @Override
    public int getOrder() {
        return 700;
    }
    
    /**
     * BlockStateEnabled
     * 
     * ...
     * 
     * default: true
     */
    @IsConfigurationNode(order = 100)
    public boolean getEnabled() {
        return config.getBoolean("enabled", true);
    }
    
    /**
     * BlockStateTool
     * 
     * The id or technical name (http://tinyurl.com/bukkit-material) of an item that displays information about the
     * right-clicked block.
     * 
     * default: WOOD_AXE
     */
    @IsConfigurationNode(order = 200)
    public Material getToolType() {
        if (config.isString("tool")) {
            Material v = Material.getMaterial(config.getString("tool"));
            if (v != null)
                return v;
        } else if (config.isInt("tool")) {
            Material v = Material.getMaterial(config.getInt("tool"));
            if (v != null)
                return v;
        } else {
            Object v = config.get("tool", Material.WOOD_AXE);
            if (v instanceof Material)
                return (Material) v;
        }
        mod.getLog().warn("Unknown BlockStateTool: " + config.get("tool"));
        return Material.WOOD_AXE;
    }
    
}
