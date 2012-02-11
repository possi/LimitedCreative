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

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.jaschastarke.minecraft.worldguard.events.PlayerChangedAreaEvent;

public class CListener implements Listener {
    private Interface com;
    public CListener(Interface com) {
        this.com = com;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled())
            return;
        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) { // he really moved, and not just looked around

            if (com.getRegionManager().isDiffrentRegion(event.getPlayer(), event.getTo())) {
                Bukkit.getServer().getPluginManager().callEvent(new PlayerChangedAreaEvent(event));
                CPlayer.get(event.getPlayer()).setHash(com.getRegionManager().getRegionsHash(event.getTo()));
            }
        }
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled())
            return;
        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) { // he really moved, and not just looked around
            
            if (com.getRegionManager().isDiffrentRegion(event.getPlayer(), event.getTo())) {
                Bukkit.getServer().getPluginManager().callEvent(new PlayerChangedAreaEvent(event));
                CPlayer.get(event.getPlayer()).setHash(com.getRegionManager().getRegionsHash(event.getTo()));
            }
        }
    }
    
    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event) {
        CPlayer.get(event.getPlayer()).setHash(com.getRegionManager().getRegionsHash(event.getPlayer().getLocation()));
    }
    
    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        CPlayer.remove(event.getPlayer());
    }
}
