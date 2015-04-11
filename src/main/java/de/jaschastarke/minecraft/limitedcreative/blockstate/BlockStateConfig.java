package de.jaschastarke.minecraft.limitedcreative.blockstate;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import de.jaschastarke.bukkit.lib.configuration.Configuration;
import de.jaschastarke.bukkit.lib.configuration.ConfigurationContainer;
import de.jaschastarke.bukkit.lib.configuration.StringList;
import de.jaschastarke.configuration.IConfigurationNode;
import de.jaschastarke.configuration.IConfigurationSubGroup;
import de.jaschastarke.configuration.InvalidValueException;
import de.jaschastarke.configuration.annotations.IsConfigurationNode;
import de.jaschastarke.maven.ArchiveDocComments;
import de.jaschastarke.maven.PluginConfigurations;
import de.jaschastarke.minecraft.limitedcreative.Config;
import de.jaschastarke.minecraft.limitedcreative.ModBlockStates;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;

/**
 * BlockState-Feature
 * 
 * http://dev.bukkit.org/server-mods/limited-creative/pages/features/blockstate/
 */
@ArchiveDocComments
@PluginConfigurations(parent = Config.class)
public class BlockStateConfig extends Configuration implements IConfigurationSubGroup {
    protected ModBlockStates mod;
    protected ModuleEntry<IModule> entry;

    public BlockStateConfig(ConfigurationContainer container) {
        super(container);
    }
    public BlockStateConfig(ModBlockStates mod, ModuleEntry<IModule> modEntry) {
        super(mod.getPlugin().getDocCommentStorage());
        this.mod = mod;
        entry = modEntry;
    }
    
    @Override
    public void setValue(IConfigurationNode node, Object pValue) throws InvalidValueException {
        if (node.getName().equals("tool"))
            setTool(pValue);
        else
            super.setValue(node, pValue);
        if (node.getName().equals("enabled")) {
            entry.setEnabled(getEnabled());
        } else if (node.getName().equals("useThreading")) {
            if (entry.isEnabled()) {
                entry.disable();
                entry.enable();
            }
        }
    }
    
    @Override
    public void setValues(ConfigurationSection sect) {
        ignoredWorlds = null;
        super.setValues(sect);
        entry.setDefaultEnabled(getEnabled());
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
     * This Feature stores the GameMode a Block was created in, and prevents drops if a Block was created 
     * in creative mode. 
     * 
     * Due to the huge load of this Feature, it isn't enabled by default. It uses the Database-credentials from 
     * bukkit.yml (http://wiki.bukkit.org/Bukkit.yml#database) in the server-directory.
     * 
     * default: false
     */
    @IsConfigurationNode(order = 100)
    public boolean getEnabled() {
        return config.getBoolean("enabled", false);
    }
    
    /**
     * BlockStateThreading
     * 
     * Uses Threading to minimize lag. This fully relies on Bukkit metadata implementation. You only should need this,
     * if there are often plays more then 10 players at once on your server, or you're about to use huge WorldEdits often.
     * Be aware that this requires more memory, to increase the performance
     * 
     * Without threading, huge WorldEdits becomes much noticeable slower.
     * 
     * default: true
     */
    @IsConfigurationNode(order = 150)
    public boolean getUseThreading() {
        return config.getBoolean("useThreading", true);
    }
    
    /**
     * BlockStateTool
     * 
     * The id or technical name (http://public.ja-s.de/bukkit/material) of an item that displays information about the 
     * right-clicked block.
     * 
     * default: WOOD_PICKAXE
     */
    @IsConfigurationNode(order = 200)
    public Material getTool() {
        if (config.isString("tool")) {
            Material v = Material.getMaterial(config.getString("tool"));
            if (v != null)
                return v;
        } else if (config.isInt("tool")) {
            Material v = Material.getMaterial(config.getInt("tool"));
            if (v != null)
                return v;
        } else {
            Object v = config.get("tool", Material.WOOD_PICKAXE);
            if (v instanceof Material)
                return (Material) v;
        }
        mod.getLog().warn("Unknown BlockStateTool: " + config.get("tool"));
        return Material.WOOD_PICKAXE;
    }
    
    /**
     * BlockStateLogSurvival
     * 
     * Log all Block-Places to the database. Disable to make the database more slim by not adding blocks placed in 
     * survival-mode.
     * 
     * default: false
     */
    @IsConfigurationNode(order = 400)
    public boolean getLogSurvival() {
        return config.getBoolean("logSurvival", false);
    }
    
    private StringList ignoredWorlds = null;
    
    /**
     * BlockStateIgnoredWorlds
     * 
     * While you may use per world permissions to configure limitations fine graded, you may want to disable the 
     * BlockState-Feature for certain worlds (like complete creative worlds) to save cpu and memory.
     * 
     * default: []
     */
    @IsConfigurationNode(order = 500)
    public StringList getIgnoredWorlds() {
        if (ignoredWorlds == null) {
            ignoredWorlds = new StringList(config.getStringList("ignoredWorlds"));
        }
        return ignoredWorlds;
    }
    
    /**
     * BlockStateWorldEditIntegration
     * 
     * Allows you to disable hook into WorldEdit for better Performance. By default Integration is enable, so it logs 
     * block modifications via worldedit as creative-placed blocks in the database.
     * A server reload (better restart) is needed to disabled WE-Integration.
     * 
     * default: true
     */
    @IsConfigurationNode(order = 600)
    public boolean getWorldeditIntegration() {
        return config.getBoolean("worldeditIntegration", true);
    }
    
    protected void setTool(Object val) throws InvalidValueException {
        String v = (String) val;
        Material m = null;
        try {
            int i = Integer.parseInt(v);
            if (i > 0)
                m = Material.getMaterial(i);
        } catch (NumberFormatException e) {
            m = null;
        }
        if (m == null)
            m = Material.getMaterial(v);
        if (m == null)
            throw new InvalidValueException("Material '" + v + "' not found");
        else
            config.set("tool", m);
    }


    @Override
    public Object getValue(final IConfigurationNode node) {
        Object val = super.getValue(node);
        if (node.getName().equals("tool") && val != null) {
            return val.toString();
        } else {
            return val;
        }
    }
}
