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

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import de.jaschastarke.minecraft.limitedcreative.store.PlayerOptions;

public class Players {
    public static final long CLEANUP_TIMEOUT = 300000; // 300s = 5m
    private static Map<String, LCPlayer> players = new HashMap<String, LCPlayer>();
    private static PlayerOptions options = new PlayerOptions();
    
    public static LCPlayer get(Player player) {
        Core.debug("player: " + player.getName() + " - " + ((Object)player).hashCode() + " - " + player.getEntityId() + " - " + player.getUniqueId());
        if (!players.containsKey(player.getName())) {
            LCPlayer p = new LCPlayer(player);
            players.put(player.getName(), p);
            return p;
        } else {
            LCPlayer p = players.get(player.getName());
            /*if (player != p.getPlayer())
                p.updatePlayer(player);*/
            p.touch();
            return p;
        }
    }
    
    public static LCPlayer get(String player) {
        if (players.containsKey(player)) {
            return players.get(player);
        }
        return null;
    }
    
    public static void remove(String player) {
        players.remove(player);
    }
    
    /*public static void clear(String player) {
        if (players.containsKey(player)) {
            LCPlayer p = players.get(player);
            p.updatePlayer(null);
            p.touch(); // keep meta data alive till cleanup, but remove player bukkit assoc.
        }
    }*/
    
    /*public static void cleanUp() {
        int count = players.size();
        Iterator<Map.Entry<String, LCPlayer>> i = players.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, LCPlayer> entry = i.next();
            if (entry.getValue().isOutdated()) {
                Core.debug("removing "+entry.getValue().getName());
                i.remove();
            }
        }
        Core.debug("cleanup done: player count: "+count+" / "+players.size());
    }*/

    public static PlayerOptions getOptions() {
        return options;
    }
}
