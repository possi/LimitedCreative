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
package de.jaschastarke.minecraft.limitedcreative.regions.worldguard;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.jaschastarke.minecraft.limitedcreative.ModRegions;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.events.PlayerChangedAreaEvent;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.events.PlayerNewLocationAreaEvent;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.events.PlayerSetAreaEvent;

public class PlayerRegionListener implements Listener {
    private ModRegions mod;
    
    public PlayerRegionListener(ModRegions mod) {
        this.mod = mod;
    }
    
    @EventHandler(priority=EventPriority.HIGHEST) // run very late, because the event may be cancelled
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled())
            return;
        
        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ()
                || !event.getFrom().getWorld().equals(event.getTo().getWorld())) { // he really moved, and not just looked around

            String current_hash = mod.getPlayerData(event.getPlayer()).getHash();
            if (current_hash == null) {
                String new_hash = mod.getRegionManager().getRegionsHash(event.getTo());
                Bukkit.getServer().getPluginManager().callEvent(new PlayerSetAreaEvent(mod.getRegionManager(), event.getPlayer(), new_hash, event.getTo()));
                mod.getPlayerData(event.getPlayer()).setHash(new_hash);
            } else if (mod.getRegionManager().isDiffrentRegion(event.getPlayer(), event.getTo())) {
                String new_hash = mod.getRegionManager().getRegionsHash(event.getTo());
                PlayerChangedAreaEvent areaevent = new PlayerChangedAreaEvent(mod.getRegionManager(), event, current_hash, new_hash);
                Bukkit.getServer().getPluginManager().callEvent(areaevent);
                if (!areaevent.isCancelled())
                    mod.getPlayerData(event.getPlayer()).setHash(new_hash);
            }
        }
    }
    
    @EventHandler(priority=EventPriority.HIGHEST) // run very late, because the event may be cancelled
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        onPlayerMove(event);
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        String new_hash = mod.getRegionManager().getRegionsHash(event.getRespawnLocation());
        PlayerNewLocationAreaEvent areaevent = new PlayerNewLocationAreaEvent(mod.getRegionManager(), event.getPlayer(), event.getRespawnLocation(), new_hash);
        Bukkit.getServer().getPluginManager().callEvent(areaevent);
        mod.getPlayerData(event.getPlayer()).setHash(new_hash);
    }
    
    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event) {
        String new_hash = mod.getRegionManager().getRegionsHash(event.getPlayer().getLocation());
        Bukkit.getServer().getPluginManager().callEvent(new PlayerSetAreaEvent(mod.getRegionManager(), event.getPlayer(), new_hash));
        mod.getPlayerData(event.getPlayer()).setHash(new_hash);
    }
    
    /* Thanks to WeakReference not longer needed
    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        CPlayer.remove(event.getPlayer());
    }*/
    
    @EventHandler
    public void onPlayerComamnd(PlayerCommandPreprocessEvent event) {
        String[] cmd = event.getMessage().split(" ");
        if (cmd.length >= 2) {
            if (cmd[0].replaceAll("/", "").equalsIgnoreCase("region")) {
                if (cmd[1].equalsIgnoreCase("addowner") || cmd[1].equalsIgnoreCase("addmember") ||
                        cmd[1].equalsIgnoreCase("removeowner") || cmd[1].equalsIgnoreCase("remowner") ||
                        cmd[1].equalsIgnoreCase("removemember") || cmd[1].equalsIgnoreCase("remmember") ||
                        cmd[1].equalsIgnoreCase("removemem") || cmd[1].equalsIgnoreCase("remmem")) {
                    mod.getPlayerData().clearAllTemp();
                }
            }
        }
    }
}
