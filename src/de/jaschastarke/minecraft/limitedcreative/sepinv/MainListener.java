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
package de.jaschastarke.minecraft.limitedcreative.sepinv;

import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import de.jaschastarke.minecraft.limitedcreative.Core;
import de.jaschastarke.minecraft.limitedcreative.Players;

public class MainListener implements Listener {
    private Core plugin;
    public MainListener(Core plugin) {
        this.plugin = plugin;
    }
    
    /**
     * The isCancelled in PlayerInteractEvent doesn't check useItemInHand, even this decides (when clicking on
     * entity with e.g. a bucket)
     * @param event
     * @return The relevant "isCancelled"
     */
    public static boolean isCancelled(PlayerInteractEvent event) {
        return event.useInteractedBlock() == Event.Result.DENY && event.useItemInHand() == Event.Result.DENY;
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
    
    /**
     * Also needed if WorldGuard-Feature is enabled, so can not moved to optional Listener "Limit".
     */
    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (event.isCancelled())
            return;
        if (event.getEntity() instanceof Item) {
            if (plugin.spawnblock.isBlocked(event.getLocation().getBlock().getLocation(), ((Item) event.getEntity()).getItemStack().getType())) {
                event.setCancelled(true);
            }
        }
    }
    
    public void onLogout(PlayerQuitEvent event) {
        // what? i can't cancel a logout event? but how to chain the user to the server than? xD
        Players.remove(event.getPlayer().getName());
    }
}
