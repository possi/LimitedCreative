/*
 * Limited Creative - (Bukkit Plugin)
 * Copyright (C) 2012 jascha@ja-s.de
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.jaschastarke.minecraft.limitedcreative.serialize;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
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
        if (!LimitedCreativeCore.serializeFallBack && !LimitedCreativeCore.plugin.config.getUnsafeStorage()) {
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
        } else {
            ConfigurationSection s = section.getConfigurationSection(path);
            Map<String, Object> serialize = s.getValues(false);
            serialize.remove("enchantments");
            if (s.contains("damage") && LimitedCreativeCore.serializeFallBack)
                serialize.put("damage", new Integer(s.getInt("damage")).shortValue());
            ItemStack result = ItemStack.deserialize(serialize);
            Map<String, Object> item = section.getConfigurationSection(path).getValues(false);
            item.remove("enchantments");
            if (s.contains("enchantments")) {
                for (Map.Entry<String, Object> entry : s.getConfigurationSection("enchantments").getValues(false).entrySet()) {
                    Enchantment enchantment = Enchantment.getByName(entry.getKey().toString());
                    if ((enchantment != null) && (entry.getValue() instanceof Integer)) {
                        result.addUnsafeEnchantment(enchantment, (Integer) entry.getValue());
                    }
                }
            }
            return result;
        }
    }
    public static Map<Integer, ItemStack> storeInventory(PlayerInventory inv) {
        Map<Integer, ItemStack> map = new HashMap<Integer, ItemStack>();
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) != null && inv.getItem(i).getTypeId() != 0) {
                map.put(i, inv.getItem(i));
            }
        }
        for (int i = 0; i < inv.getArmorContents().length; i++) {
            map.put((i * -1) - 1, inv.getArmorContents()[i]);
        }
        return map;
    }
    public static void restoreInventory(PlayerInventory inv, Map<Integer, ItemStack> map) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (map.containsKey(i)) {
                inv.setItem(i, map.get(i));
            } else {
                inv.setItem(i, null);
            }
        }
        for (int i = 0; i < inv.getArmorContents().length; i++) {
            int _i = (i * -1) - 1;
            if (map.containsKey(_i)) {
                inv.getArmorContents()[i] = map.get(_i);
            } else {
                inv.getArmorContents()[i] = null;
            }
        }
    }
}
