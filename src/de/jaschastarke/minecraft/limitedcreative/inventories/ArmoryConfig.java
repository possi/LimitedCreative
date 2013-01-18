package de.jaschastarke.minecraft.limitedcreative.inventories;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.material.MaterialData;

import de.jaschastarke.bukkit.lib.ModuleLogger;
import de.jaschastarke.bukkit.lib.configuration.Configuration;
import de.jaschastarke.bukkit.lib.items.MaterialDataNotRecognizedException;
import de.jaschastarke.bukkit.lib.items.MaterilNotRecognizedException;
import de.jaschastarke.bukkit.lib.items.Utils;
import de.jaschastarke.configuration.annotations.IConfigurationSubGroup;
import de.jaschastarke.configuration.annotations.IsConfigurationNode;
import de.jaschastarke.maven.ArchiveDocComments;
import de.jaschastarke.minecraft.limitedcreative.LimitedCreative;
import de.jaschastarke.minecraft.limitedcreative.ModInventories;

/**
 * CreativeArmor
 * 
 * When set, all creative Player automatically wears the given items as Armor. So they are better seen by other Players.
 */
@ArchiveDocComments
public class ArmoryConfig extends Configuration implements IConfigurationSubGroup {
    protected ModInventories mod;
    public ArmoryConfig(ModInventories modInventories) {
        mod = modInventories;
    }
    @Override
    public String getNodeName() {
        return "armor";
    }

    /**
     * CreativeArmorEnabled
     * 
     * When disabled, the players Armor isn't swapped
     * 
     * default: true
     */
    @IsConfigurationNode
    public boolean getEnabled() {
        return config.getBoolean("enabled", true);
    }
    
    /**
     * CreativeArmor-Items
     * 
     * Allows changing of the "Creative-Armor" to be wear when in creative mode
     * 
     * *see Blacklist for details on Item-Types
     */
    public Map<String, MaterialData> getCreativeArmor() {
        if (getEnabled()) {
            Map<String, MaterialData> armor = new HashMap<String, MaterialData>();
            for (Map.Entry<String, Object> entry : config.getValues(false).entrySet()) {
                if (!entry.getKey().equals("enabled")) {
                    MaterialData md = null;
                    try {
                        md = Utils.parseMaterial((String) entry.getValue());
                    } catch (MaterilNotRecognizedException e) {
                        getLog().warn(L("exception.config.material_not_found", entry.getValue()));
                    } catch (MaterialDataNotRecognizedException e) {
                        getLog().warn(L("exception.config.materiak_data_not_found", entry.getValue()));
                    }
                    
                    if (md != null)
                        armor.put(entry.getKey(), md);
                }
            }
            return armor.size() > 0 ? armor : null;
        }
        return null;
    }

    @Deprecated
    public String L(String msg, Object... objects) {
        return ((LimitedCreative) Bukkit.getPluginManager().getPlugin("LimitedCreative")).getLocale().trans(msg, objects);
    }
    @Deprecated
    public ModuleLogger getLog() {
        return ((LimitedCreative) Bukkit.getPluginManager().getPlugin("LimitedCreative")).getModule(ModInventories.class).getLog();
    }
}
