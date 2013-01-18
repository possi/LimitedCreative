package de.jaschastarke.minecraft.limitedcreative.inventories;

import de.jaschastarke.bukkit.lib.configuration.Configuration;
import de.jaschastarke.configuration.annotations.IConfigurationSubGroup;
import de.jaschastarke.configuration.annotations.IsConfigurationNode;
import de.jaschastarke.maven.ArchiveDocComments;
import de.jaschastarke.minecraft.limitedcreative.ModInventories;

@ArchiveDocComments
public class InventoryConfig extends Configuration implements IConfigurationSubGroup {
    protected ModInventories mod;
    public InventoryConfig(ModInventories modInventories) {
        mod = modInventories;
    }
    @Override
    public String getNodeName() {
        return "store";
    }
    
    /**
     * SeparatedInventoryEnabled
     * 
     * Use this option to disable the separated inventories feature, for the case you only need the other features.
     * 
     * default: true
     */
    @IsConfigurationNode
    public boolean getEnabled() {
        return config.getBoolean("enabled", true);
    }
    
    /**
     * StoreCreative
     * 
     * Should the creative-inventory also be stored on disk, when switching to survival?
     * If disabled, the inventory gets cleared every time on switching to creative.
     * 
     * default: true
     */
    @IsConfigurationNode
    public boolean getStoreCreative() {
        return config.getBoolean("storeCreative", config.getBoolean("creative", true));
    }
    
    /**
     * SeparateAdventureInventory
     * 
     * When true, your players get a separate inventory when switching to adventure gamemode (2). Otherwise
     * they have the default survival inventory while in adventure gamemode.
     * 
     * default: false
     */
    @IsConfigurationNode
    public boolean getSeparateAdventure() {
        return config.getBoolean("separateAdventure", config.getBoolean("adventure", true));
    }
    
    /**
     * InventoryFolder
     * 
     * The folder inside the datadir-folder (plugin/LimitedCreative) where the inventories are saved to.
     * By default the inventories are saved to "plugin/LimitedCreative/inventories".
     * 
     * default: "inventories"
     */
    @IsConfigurationNode
    public String getFolder() {
        return config.getString("folder", "inventories");
    }

}
