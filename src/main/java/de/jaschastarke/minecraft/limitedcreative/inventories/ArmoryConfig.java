package de.jaschastarke.minecraft.limitedcreative.inventories;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import de.jaschastarke.bukkit.lib.ModuleLogger;
import de.jaschastarke.bukkit.lib.configuration.Configuration;
import de.jaschastarke.bukkit.lib.items.MaterialDataNotRecognizedException;
import de.jaschastarke.bukkit.lib.items.MaterialNotRecognizedException;
import de.jaschastarke.bukkit.lib.items.ItemUtils;
import de.jaschastarke.configuration.IConfigurationSubGroup;
import de.jaschastarke.configuration.annotations.IsConfigurationNode;
import de.jaschastarke.maven.ArchiveDocComments;
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
        super(modInventories.getPlugin());
        mod = modInventories;
    }

    @Override
    public void setValues(ConfigurationSection sect) {
        if (sect == null || sect.getValues(false).size() == 0) {
            ConfigurationSection parent_sect = mod.getConfig().getValues();
            if (parent_sect.contains("armore") && parent_sect.isConfigurationSection("armor")) {
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
    public Map<String, ItemStack> getCreativeArmor() {
        if (getEnabled()) {
            Map<String, ItemStack> armor = new HashMap<String, ItemStack>();
            for (Map.Entry<String, Object> entry : config.getValues(false).entrySet()) {
                if (!entry.getKey().equals("enabled")) {
                    if (entry.getValue() instanceof ItemStack) {
                        armor.put(entry.getKey(), (ItemStack) entry.getValue());
                    } else {
                        MaterialData md = null;
                        try {
                            md = ItemUtils.parseMaterial((String) entry.getValue());
                        } catch (MaterialNotRecognizedException e) {
                            getLog().warn(L("exception.config.material_not_found", entry.getValue()));
                        } catch (MaterialDataNotRecognizedException e) {
                            getLog().warn(L("exception.config.materiak_data_not_found", entry.getValue()));
                        }
                        
                        if (md != null)
                            armor.put(entry.getKey(), md.toItemStack(1));
                    }
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
    public Object getHead() {
        return config.get("head", "CHAINMAIL_HELMET");
    }
    @IsConfigurationNode(order = 501)
    public Object getChest() {
        return config.get("chest", "CHAINMAIL_CHESTPLATE");
    }
    @IsConfigurationNode(order = 502)
    public Object getLegs() {
        return config.get("legs", "CHAINMAIL_LEGGINGS");
    }
    @IsConfigurationNode(order = 503)
    public Object getFeet() {
        return config.get("feet", "CHAINMAIL_BOOTS");
    }

    public String L(String msg, Object... objects) {
        return mod.getPlugin().getLocale().trans(msg, objects);
    }
    public ModuleLogger getLog() {
        return mod.getLog();
    }
}
