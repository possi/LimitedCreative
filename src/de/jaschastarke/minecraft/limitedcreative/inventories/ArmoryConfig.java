package de.jaschastarke.minecraft.limitedcreative.inventories;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.material.MaterialData;

import de.jaschastarke.bukkit.lib.ModuleLogger;
import de.jaschastarke.bukkit.lib.configuration.Configuration;
import de.jaschastarke.bukkit.lib.items.MaterialDataNotRecognizedException;
import de.jaschastarke.bukkit.lib.items.MaterilNotRecognizedException;
import de.jaschastarke.bukkit.lib.items.Utils;
import de.jaschastarke.configuration.IConfigurationSubGroup;
import de.jaschastarke.configuration.annotations.IsConfigurationNode;
import de.jaschastarke.maven.ArchiveDocComments;
import de.jaschastarke.minecraft.limitedcreative.LimitedCreative;
import de.jaschastarke.minecraft.limitedcreative.ModInventories;

/**
 * InventoryCreativeArmor
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
    public void setValues(ConfigurationSection sect) {
        if (sect == null || sect.getValues(false).size() == 0) {
            ConfigurationSection parent_sect = mod.getConfig().getValues();
            if (parent_sect.contains("armor")) {
                sect = parent_sect.createSection(this.getName(), parent_sect.getConfigurationSection("armor").getValues(true));
            }
        }
        super.setValues(sect);
    }
    @Override
    public String getName() {
        return "creativeArmor";
    }
    @Override
    public int getOrder() {
        return 1000;
    }

    /**
     * InventoryCreativeArmorEnabled
     * 
     * When disabled, the players Armor isn't changed.
     * 
     * default: true
     */
    @IsConfigurationNode
    public boolean getEnabled() {
        return config.getBoolean("enabled", true);
    }
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
    
    /**
     * InventoryCreativeArmorItems
     * 
     * Allows changing of the "Creative-Armor" to be wear when in creative mode
     * 
     * *see Blacklist for details on Item-Types
     */
    @IsConfigurationNode(order = 500)
    public String getHead() {
        return config.getString("head", "CHAINMAIL_HELMET");
    }
    @IsConfigurationNode(order = 501)
    public String getChest() {
        return config.getString("chest", "CHAINMAIL_CHESTPLATE");
    }
    @IsConfigurationNode(order = 502)
    public String getLegs() {
        return config.getString("legs", "CHAINMAIL_LEGGINGS");
    }
    @IsConfigurationNode(order = 503)
    public String getFeet() {
        return config.getString("feet", "CHAINMAIL_BOOTS");
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
