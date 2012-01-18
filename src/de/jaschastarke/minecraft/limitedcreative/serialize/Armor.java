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
