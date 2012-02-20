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

import org.bukkit.configuration.MemoryConfiguration;

import de.jaschastarke.minecraft.limitedcreative.Inventory;
import de.jaschastarke.minecraft.limitedcreative.Inventory.Target;

public class InvMemStorage extends InvConfStorage {
    private MemoryConfiguration storage = new MemoryConfiguration();
    
    @Override
    public void store(Inventory pinv, Target target) {
        store(pinv, storage.createSection(pinv.getPlayer().getName()));
    }
    
    @Override
    public void load(Inventory pinv, Target target) {
        load(pinv, storage.getConfigurationSection(pinv.getPlayer().getName()));
    }

    @Override
    public void remove(Inventory pinv, Target target) {
        storage.set(pinv.getPlayer().getName(), null);
    }

    @Override
    public boolean contains(Inventory pinv, Target target) {
        return storage.contains(pinv.getPlayer().getName());
    }
}
