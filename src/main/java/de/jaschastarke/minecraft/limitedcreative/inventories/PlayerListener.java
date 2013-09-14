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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import de.jaschastarke.minecraft.limitedcreative.Hooks;
import de.jaschastarke.minecraft.limitedcreative.ModInventories;

public class PlayerListener implements Listener {
    private ModInventories mod;
    public PlayerListener(ModInventories mod) {
        this.mod = mod;
    }
    
    @EventHandler(priority=EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        boolean isLoggedIn = Hooks.IsLoggedIn.test(event.getPlayer());
        if (mod.isDebug()) {
            mod.getLog().debug("onPlayerGameModeChange: "+event.getPlayer().getName());
            mod.getLog().debug("Current GameMode: "+event.getPlayer().getGameMode());
            mod.getLog().debug("New GameMode: "+event.getNewGameMode());
            mod.getLog().debug("isLoggedin: "+isLoggedIn);
            mod.getLog().debug("isCancelled: "+event.isCancelled());
        }
        if (!isLoggedIn)
            return;
        
        mod.onSetGameMode(event.getPlayer(), event.getNewGameMode());
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            mod.setCreativeArmor(event.getPlayer());
        }
    }
}
