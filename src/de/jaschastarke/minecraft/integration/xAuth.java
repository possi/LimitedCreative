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
package de.jaschastarke.minecraft.integration;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.cypherx.xauth.xAuthPlayer;

import de.jaschastarke.minecraft.limitedcreative.Core;

public class xAuth implements CommunicationBridge {
    public static boolean isLoggedInNotGuest(Player player) {
        xAuthPlayer xpl = getAuth().getPlayerManager().getPlayer(player);
        boolean li = true;
        if (!xpl.isAuthenticated())
            li = false;
        else if (xpl.isGuest())
            li = false;
        Core.debug("xAuth: "+player.getName()+": logged in not guest: "+li);
        return li;
    }
    
    private static com.cypherx.xauth.xAuth getAuth() {
        return (com.cypherx.xauth.xAuth) Bukkit.getServer().getPluginManager().getPlugin("xAuth");
    }
}
