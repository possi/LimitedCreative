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
package de.jaschastarke.minecraft.limitedcreative.gmperm;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import de.jaschastarke.minecraft.limitedcreative.ModGameModePerm;

public class PlayerListener implements Listener {
    private ModGameModePerm mod;
    public PlayerListener(ModGameModePerm mod) {
        this.mod = mod;
    }
    
    protected Permission v() {
        return mod.getPermissionInterface().getPermission();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        if (event.isCancelled())
            return;
        
        ensureGroup(event.getPlayer(), event.getNewGameMode());
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() == Result.ALLOWED)
            return;
        
        ensureGroup(event.getPlayer());
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            ensureGroup(event.getPlayer());
        }
    }
    
    protected void ensureGroup(Player player) {
        ensureGroup(player, player.getGameMode());
    }
    protected void ensureGroup(Player player, GameMode gm) {
        String cgroup = mod.getConfig().getCreativeGroup();
        String agroup = mod.getConfig().getAdventureGroup();

        if (gm == GameMode.CREATIVE) {
            if (!v().playerInGroup(player, cgroup)) {
                v().playerAddGroup(player, cgroup);
            }
            if (agroup != null && v().playerInGroup(player, agroup)) {
                v().playerRemoveGroup(player, agroup);
            }
        } else if (gm == GameMode.ADVENTURE) {
            if (v().playerInGroup(player, cgroup)) {
                v().playerRemoveGroup(player, cgroup);
            }
            if (agroup != null && !v().playerInGroup(player, agroup)) {
                v().playerAddGroup(player, agroup);
            }
        } else if (gm == GameMode.SURVIVAL) {
            if (v().playerInGroup(player, cgroup)) {
                v().playerRemoveGroup(player, cgroup);
            }
            if (agroup != null && v().playerInGroup(player, agroup)) {
                v().playerRemoveGroup(player, agroup);
            }
        }
    }
}
