package de.jaschastarke.minecraft.limitedcreative;

import org.bukkit.configuration.ConfigurationSection;

import de.jaschastarke.bukkit.lib.Core;
import de.jaschastarke.bukkit.lib.configuration.PluginConfiguration;
import de.jaschastarke.configuration.IConfigurationNode;
import de.jaschastarke.configuration.InvalidValueException;
import de.jaschastarke.configuration.annotations.IsConfigurationNode;
import de.jaschastarke.maven.ArchiveDocComments;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;
import de.jaschastarke.modularize.ModuleEntry.ModuleState;

/**
 * Limited Creative - Configuration
 * 
 * (YAML-Syntax: http://en.wikipedia.org/wiki/YAML)
 * 
 * This configuration-file is automatically written when changed via ingame-commands. So any manual added comments are 
 * removed.
 */
@ArchiveDocComments
public class Config extends PluginConfiguration {
    public Config(Core plugin) {
        super(plugin);
    }
    
    @Override
    public void setValues(ConfigurationSection sect) {
        super.setValues(sect);
        
        if (plugin.getModules().size() > 0) {
            setModuleStates();
        }
    }
    
    public void setModuleStates() {
        ModuleEntry<IModule> metricsEntry = plugin.getModule(FeatureMetrics.class).getModuleEntry();
        if (metricsEntry.initialState != ModuleState.NOT_INITIALIZED)
            metricsEntry.initialState = getMetrics() ? ModuleState.ENABLED : ModuleState.DISABLED;
    }
    
    @Override
    public void setValue(IConfigurationNode node, Object pValue) throws InvalidValueException {
        super.setValue(node, pValue);
        
        if (node.getName().equals("metrics")) {
            ModuleEntry<IModule> metricsEntry = plugin.getModule(FeatureMetrics.class).getModuleEntry();
            if (getMetrics()) {
                if (metricsEntry.initialState != ModuleState.NOT_INITIALIZED)
                    metricsEntry.enable();
            } else {
                metricsEntry.disable();
            }
        }
    }

    /**
     * Metrics
     * 
     * This settings allows the Addon-Author to track the Servers using this plugin. It will not track any player 
     * related data like names, ips, online time or such. Please do not disable the option! As more servers are using 
     * the plugin and the author knows, as more he is willing to support the plugin! Its a win-win for both.
     * 
     * default: true
     * @TODO Move to a sub-class modular configuration 
     */
    @IsConfigurationNode(order = 1000)
    public boolean getMetrics() {
        return config.getBoolean("metrics", true);
    }
    
    /**
     * Debug
     * 
     * The debug modus spams much details about the plugin to the server-log (console) which can help to solve issues.
     * 
     * default: false
     */
    @IsConfigurationNode(order = 9999)
    public boolean getDebug() {
        return config.getBoolean("debug", false);
    }
}
