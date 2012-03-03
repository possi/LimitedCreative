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

import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.jaschastarke.minecraft.limitedcreative.Core;
import de.jaschastarke.minecraft.limitedcreative.Players;

public class GenericListener implements Listener {
    private Core plugin;
    public GenericListener(Core plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Needed by 3 of 4 features.
     *  - Creative Limits
     *  - Creative Blocks in Survival
     *  - Regions
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
    
    /**
     * The isCancelled in PlayerInteractEvent doesn't check useItemInHand, even this decides (when clicking on
     * entity with e.g. a bucket)
     * @param event
     * @return The relevant "isCancelled"
     */
    public static boolean isCancelled(PlayerInteractEvent event) {
        return event.useInteractedBlock() == Event.Result.DENY && event.useItemInHand() == Event.Result.DENY;
    }
}
