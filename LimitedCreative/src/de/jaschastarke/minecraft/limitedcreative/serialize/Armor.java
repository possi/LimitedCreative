package de.jaschastarke.minecraft.limitedcreative.serialize;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.PlayerInventory;

public class Armor implements Storeable {
    private PlayerInventory inv;
    public Armor(PlayerInventory pi) {
        inv = pi;
    }
    
    @Override
    public void store(ConfigurationSection section) {
        if (inv.getHelmet() != null && inv.getHelmet().getTypeId() != 0)
            Items.sectionSetItem(section, "helmet", inv.getHelmet());
        if (inv.getChestplate() != null && inv.getChestplate().getTypeId() != 0)
            Items.sectionSetItem(section, "chestplate", inv.getChestplate());
        if (inv.getLeggings() != null && inv.getLeggings().getTypeId() != 0)
            Items.sectionSetItem(section, "leggins", inv.getLeggings());
        if (inv.getBoots() != null && inv.getBoots().getTypeId() != 0)
            Items.sectionSetItem(section, "boots", inv.getBoots());
    }

    @Override
    public void restore(ConfigurationSection section) {
        if (section.contains("helmet"))
            inv.setHelmet(Items.sectionGetItem(section, "helmet"));
        else
            inv.setHelmet(null);
        
        if (section.contains("chestplate"))
            inv.setChestplate(Items.sectionGetItem(section, "chestplate"));
        else
            inv.setChestplate(null);
        
        if (section.contains("leggins"))
            inv.setLeggings(Items.sectionGetItem(section, "leggins"));
        else
            inv.setLeggings(null);
        
        if (section.contains("boots"))
            inv.setBoots(Items.sectionGetItem(section, "boots"));
        else
            inv.setBoots(null);
    }

}
