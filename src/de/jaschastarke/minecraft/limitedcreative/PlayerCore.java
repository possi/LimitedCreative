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

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class PlayerCore {
    private static LimitedCreativeCore plugin;
    private Player player;

    public PlayerCore(LimitedCreativeCore pplugin, Player pplayer) {
        plugin = pplugin;
        player = pplayer;
    }
    
    public void onSetCreative() {
        if (plugin.config.getPermissionToKeepInventory() && player.hasPermission("limitedcreative.keepinventory"))
            return;
        Inventory inv = new Inventory(player);
        inv.save();
        if (plugin.config.getStoreCreative() && inv.isStored(GameMode.CREATIVE)) {
            inv.load(GameMode.CREATIVE);
        } else {
            inv.clear();
        }
    }
    public void onSetSurvival() {
        if (plugin.config.getPermissionToKeepInventory() && player.hasPermission("limitedcreative.keepinventory"))
            return;
        Inventory inv = new Inventory(player);
        if (plugin.config.getStoreCreative()) {
            inv.save();
        }
        if (inv.isStored(GameMode.SURVIVAL))
            inv.load(GameMode.SURVIVAL);
    }
}
