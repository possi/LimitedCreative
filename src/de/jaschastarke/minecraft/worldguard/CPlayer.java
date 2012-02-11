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
package de.jaschastarke.minecraft.worldguard;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public class CPlayer {
    private static Map<String, CPlayer> players = new HashMap<String, CPlayer>();
    
    public static CPlayer get(Player p) {
        if (!players.containsKey(p.getName())) {
            CPlayer pl = new CPlayer();
            players.put(p.getName(), pl);
            return pl;
        } else {
            return players.get(p.getName());
        }
    }
    public static void remove(Player p) {
        players.remove(p.getName());
    }

    private String hash = null;
    public String getHash() {
        return hash;
    }
    public void setHash(String hash) {
        this.hash = hash;
    }
}
