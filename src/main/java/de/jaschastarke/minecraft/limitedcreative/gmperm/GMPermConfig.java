package de.jaschastarke.minecraft.limitedcreative.gmperm;

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
import de.jaschastarke.minecraft.limitedcreative.ModGameModePerm;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;

/**
 * GameMode-Permissions-Feature
 * 
 * This Feature requires Vault-Plugin to be active.
 * 
 * http://dev.bukkit.org/server-mods/limited-creative/pages/features/gmperm/
 */
@ArchiveDocComments
@PluginConfigurations(parent = Config.class)
public class GMPermConfig extends Configuration implements IConfigurationSubGroup {
    protected ModGameModePerm mod;
    protected ModuleEntry<IModule> entry;
    
    public GMPermConfig(ConfigurationContainer container) {
        super(container);
    }
    public GMPermConfig(ModGameModePerm modGameModePerm, ModuleEntry<IModule> modEntry) {
        super(modGameModePerm.getPlugin().getDocCommentStorage());
        mod = modGameModePerm;
        entry = modEntry;
    }
    
    @Override
    public void setValue(IConfigurationNode node, Object pValue) throws InvalidValueException {
        super.setValue(node, pValue);
        if (node.getName().equals("enabled")) {
            entry.setEnabled(getEnabled());
        }
    }
    
    @Override
    public void setValues(ConfigurationSection sect) {
        super.setValues(sect);
        entry.setEnabled(getEnabled());
    }
    @Override
    public String getName() {
        return "gmperm";
    }
    @Override
    public int getOrder() {
        return 600;
    }
    
    /**
     * GMPermEnabled
     * 
     * Activates that players are put in a special permission group while in creative mode.
     * 
     * default: false
     */
    @IsConfigurationNode(order = 100)
    public boolean getEnabled() {
        return config.getBoolean("enabled", false);
    }
    
    /**
     * GMPermCreativeGroup
     * 
     * Defines the Permission-Group which the player gets added to on entering creative-mode. If this value is changed 
     * the old group won't be automatically removed from players already in it. So be sure to delete the old group or 
     * remove all player of it, that they don't get stuck with that permissions.
     */
    @IsConfigurationNode(order = 200)
    public String getCreativeGroup() {
        return config.getString("creativeGroup", "");
    }
    
    /**
     * GMPermAdventureGroup
     * 
     * Like GMPermCreativeGroup but for adventure users. This is optional, so you don't have to set any group.
     * 
     * default: false
     */
    @IsConfigurationNode(order = 300)
    public String getAdventureGroup() {
        return config.getString("adventureGroup");
    }
    
    @Override
    public Object getValue(final IConfigurationNode node) {
        Object val = super.getValue(node);
        if (node.getName().equals("adventureGroup") && val == null) {
            return new Boolean(false);
        } else {
            return val;
        }
    }
}
