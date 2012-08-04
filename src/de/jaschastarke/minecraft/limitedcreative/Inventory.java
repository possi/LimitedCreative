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
package de.jaschastarke.minecraft.limitedcreative;

import static de.jaschastarke.minecraft.utils.Locale.L;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import de.jaschastarke.minecraft.limitedcreative.store.PlayerInventoryStorage;

public class Inventory {
    private static PlayerInventoryStorage storage = Core.plugin.config.getInvetoryStorage();
    //private static InvMemStorage tempinvs = new InvMemStorage();
    
    protected LCPlayer player;

    public enum Target {
        SURVIVAL,
        CREATIVE,
        ADVENTURE;
        
        public static Target getTarget(GameMode gm) {
            return Target.valueOf(gm.name());
        }
    }
    
    public Inventory(LCPlayer player) {
        this.player = player;
    }
    public LCPlayer getLCPlayer() {
        return player;
    }
    public Player getPlayer() {
        return player.getPlayer();
    }
    
    private PlayerInventory inv() {
        return player.getPlayer().getInventory();
    }
    
    public void save() {
        Core.debug(getPlayer().getName()+": store inventory: "+getPlayer().getGameMode());
        storage.store(this, Target.getTarget(getPlayer().getGameMode()));
    }
    public void save(GameMode gm) {
        Core.debug(getPlayer().getName()+": store inventory: "+gm);
        storage.store(this, Target.getTarget(gm));
    }
    
    public void load() {
        load(getPlayer().getGameMode());
    }
    
    public boolean isStored(GameMode gm) {
        return storage.contains(this, Target.getTarget(gm));
    }
    
    public void load(GameMode gm) {
        Core.debug(getPlayer().getName()+": load inventory: "+gm);
        try {
            storage.load(this, Target.getTarget(gm));
        } catch (IllegalArgumentException e) {
            if (Core.plugin.config.getUnsafeStorage()) {
                throw e;
            } else {
                getPlayer().sendMessage(ChatColor.DARK_RED + L("exception.storage.load"));
            }
        }
    }
    
    /*public void storeTemp() {
        Core.debug(getPlayer().getName()+": temp store inventory");
        tempinvs.store(this);
    }
    public void restoreTemp() {
        Core.debug(getPlayer().getName()+": temp restore inventory");
        tempinvs.load(this);
    }
    public void clearTemp() {
        Core.debug(getPlayer().getName()+": temp clear inventory");
        tempinvs.remove(this);
    }*/
    
    public void clear() {
        inv().setArmorContents(new ItemStack[0]);
        inv().clear();
    }
}
