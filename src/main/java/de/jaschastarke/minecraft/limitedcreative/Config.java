package de.jaschastarke.minecraft.limitedcreative;

import java.io.IOException;

import de.jaschastarke.bukkit.lib.Core;
import de.jaschastarke.bukkit.lib.configuration.PluginConfiguration;
import de.jaschastarke.configuration.annotations.IsConfigurationNode;
import de.jaschastarke.maven.ArchiveDocComments;
import de.jaschastarke.utils.ClassDescriptorStorage;

/**
 * Limited Creative - Configuration
 * 
 * (YAML-Syntax: http://en.wikipedia.org/wiki/YAML)
 */
@ArchiveDocComments
public class Config extends PluginConfiguration {
    public Config(Core plugin) {
        super(plugin);
    }
    @Override
    public void save() {
        try {
            ClassDescriptorStorage.load(plugin.getResource("META-INF/descriptions.jos"));
        } catch (IOException e) {
            plugin.getLog().severe("Failed to load ConfigNode-Descriptions");
        }
        super.save();
    }
    
    /**
     * Debug
     */
    @IsConfigurationNode(order = 9999)
    public boolean getDebug() {
        return config.getBoolean("debug", false);
    }
}
