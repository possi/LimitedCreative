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
package de.jaschastarke.minecraft.limitedcreative.inventories;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import de.jaschastarke.minecraft.limitedcreative.inventories.store.PlayerInventoryStorage;

public class Inventory {
    private PlayerInventoryStorage storage;
    protected Player player;

    public enum Target {
        SURVIVAL,
        CREATIVE,
        ADVENTURE,
        SPECTATOR;
        
        public static Target getTarget(GameMode gm) {
            return Target.valueOf(gm.name());
        }
    }
    
    public Inventory(PlayerInventoryStorage storage, Player player) {
        this.storage= storage;
        this.player = player;
    }
    public Player getPlayer() {
        return player.getPlayer();
    }
    private PlayerInventory inv() {
        return player.getInventory();
    }
    
    public void save() {
        save(getPlayer().getGameMode());
    }
    public void save(GameMode gm) {
        storage.getLog().debug(getPlayer().getName()+": store inventory: "+gm);
        storage.store(this, Target.getTarget(gm));
    }
    
    public void load() {
        load(getPlayer().getGameMode());
    }
    
    public boolean isStored(GameMode gm) {
        return storage.contains(this, Target.getTarget(gm));
    }
    
    public void load(GameMode gm) {
        storage.getLog().debug(getPlayer().getName()+": load inventory: "+gm);
        storage.load(this, Target.getTarget(gm));
    }

    public void clear() {
        inv().setArmorContents(new ItemStack[0]);
        inv().clear();
    }
}
