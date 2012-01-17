package de.jaschastarke.minecraft.limitedcreative.serialize;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import de.jaschastarke.minecraft.limitedcreative.LimitedCreativeCore;

public class Items implements Storeable {
    private PlayerInventory inv;
    public Items(PlayerInventory pi) {
        inv = pi;
    }

    @Override
    public void store(ConfigurationSection section) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) != null && inv.getItem(i).getTypeId() != 0)
                sectionSetItem(section, String.valueOf(i), inv.getItem(i));
        }
    }

    @Override
    public void restore(ConfigurationSection section) {
        inv.clear();
        for (int i = 0; i < inv.getSize(); i++) {
            if (section.contains(String.valueOf(i)))
                inv.setItem(i, sectionGetItem(section, String.valueOf(i)));
        }
    }

    public static void sectionSetItem(ConfigurationSection section, String path, ItemStack item) {
        if (!LimitedCreativeCore.serializeFallBack) {
            section.set(path, item);
        } else { // compatibility fallback
            Map<String, Object> serialize = item.serialize();
            if (serialize.containsKey("type") && serialize.get("type") instanceof Material)
                serialize.put("type", serialize.get("type").toString());
            section.createSection(path, serialize);
        };
    }
    public static ItemStack sectionGetItem(ConfigurationSection section, String path) {
        if (section.isItemStack(path)) {
            return section.getItemStack(path);
        } else { // compatibility fallback
            ConfigurationSection s = section.getConfigurationSection(path);
            Map<String, Object> serialize = s.getValues(false);
            if (s.contains("enchantments"))
                serialize.put("enchantments", s.getConfigurationSection("enchantments").getValues(false));
            if (s.contains("damage"))
                serialize.put("damage", new Integer(s.getInt("damage")).shortValue());
            return ItemStack.deserialize(serialize);
        }
    }
}
