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
package de.jaschastarke.minecraft.limitedcreative.store;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import de.jaschastarke.minecraft.limitedcreative.Core;
import de.jaschastarke.minecraft.limitedcreative.Inventory;

abstract public class InvConfStorage extends PlayerInventoryStorage {
    private static final int ARMOR_SIZE = 4;
    
    public void store(Inventory pinv, ConfigurationSection sect) {
        PlayerInventory inv = pinv.getPlayer().getInventory();
        sect.set("version", 2);
        if (Core.plugin.config.getUnsafeStorage())
            sect.set("unsafe", true);
        storeItems(sect.createSection("armor"), inv.getArmorContents());
        storeItems(sect.createSection("inv"), inv.getContents());
    }
    
    public void load(Inventory pinv, ConfigurationSection sect) {
        PlayerInventory inv = pinv.getPlayer().getInventory();
        
        if (!sect.contains("version")) {
            Fallback.loadVersion1(inv, sect);
        } else {
            inv.setArmorContents(restoreItems(sect.getConfigurationSection("armor"), ARMOR_SIZE));
            inv.setContents(restoreItems(sect.getConfigurationSection("inv"), inv.getSize()));
        }
    }
    
    protected void storeItems(ConfigurationSection sect, ItemStack[] items) {
        for (int i = 0; i < items.length; i++) {
            ItemStack is = items[i];
            if (is != null && is.getType() != Material.AIR) {
                sect.set(String.valueOf(i), serialize(is));
            }
        }
    }
    protected ItemStack[] restoreItems(ConfigurationSection sect, int size) {
        ItemStack[] items = new ItemStack[size];
        if (sect != null) {
            for (int i = 0; i < size; i++) {
                if (sect.contains(String.valueOf(i))) {
                    if (sect.isItemStack(String.valueOf(i))) {
                        items[i] = sect.getItemStack(String.valueOf(i));
                    } else {
                        items[i] = deserializeItemStack(sect.get(String.valueOf(i)));
                    }
                } else {
                    items[i] = null;
                }
            }
        }
        return items;
    }

    protected Object serialize(ItemStack is) {
        if (Core.plugin.config.getUnsafeStorage())
            return is.serialize();
        return is;
    }
    
    @SuppressWarnings("unchecked")
    protected ItemStack deserializeItemStack(Object is) {
        if (is instanceof ConfigurationSection) {
            ConfigurationSection sect = (ConfigurationSection) is;
            Material type = Material.getMaterial(sect.getString("type"));
            short damage = new Integer(sect.getInt("damage", 0)).shortValue();
            int amount = sect.getInt("amount", 1);
            
            ItemStack result = new ItemStack(type, amount, damage);
            if (sect.contains("enchantments")) {
                for (Map.Entry<String, Object> entry : sect.getConfigurationSection("enchantments").getValues(false).entrySet()) {
                    Enchantment enchantment = Enchantment.getByName(entry.getKey().toString());
                    if ((enchantment != null) && (entry.getValue() instanceof Integer)) {
                        result.addUnsafeEnchantment(enchantment, (Integer) entry.getValue());
                    }
                }
            }
            return result;
        } else if (is instanceof Map) {
            return ItemStack.deserialize((Map<String, Object>) is);
        } else {
            Core.plugin.warn("Failed to restore Item: "+is.toString());
            return null;
        }
    }
}
