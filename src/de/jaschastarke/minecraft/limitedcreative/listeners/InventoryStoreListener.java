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
package de.jaschastarke.minecraft.limitedcreative.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import de.jaschastarke.minecraft.limitedcreative.Core;
import de.jaschastarke.minecraft.limitedcreative.Players;

public class InventoryStoreListener implements Listener {
    private Core plugin;
    public InventoryStoreListener(Core plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        if (Core.isDebug()) {
            Core.debug("onPlayerGameModeChange: "+event.getPlayer().getName());
            Core.debug("Current GameMode: "+event.getPlayer().getGameMode());
            Core.debug("New GameMode: "+event.getNewGameMode());
            Core.debug("isLoggedin: "+plugin.com.isLoggedIn(event.getPlayer()));
            Core.debug("isCancelled: "+event.isCancelled());
        }
        if (!plugin.com.isLoggedIn(event.getPlayer()))
            return;

        if (!Players.get(event.getPlayer()).onSetGameMode(event.getNewGameMode()))
            event.setCancelled(true);
    }
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Players.get(event.getPlayer()).onRevive();
    }
    
}
