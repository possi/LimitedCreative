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
package de.jaschastarke.minecraft.limitedcreative.limits;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import de.jaschastarke.bukkit.lib.events.AttachedBlockDestroyedByPlayerEvent;
import de.jaschastarke.bukkit.lib.events.HangingBreakByPlayerBlockEvent;
import de.jaschastarke.minecraft.lib.permissions.IAbstractPermission;
import de.jaschastarke.minecraft.lib.permissions.IDynamicPermission;
import de.jaschastarke.minecraft.limitedcreative.ModCreativeLimits;

public class BlockListener implements Listener {
    private ModCreativeLimits mod;
    public BlockListener(ModCreativeLimits mod) {
        this.mod = mod;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            if (mod.getConfig().getBlockBreak().isListed(event.getBlock())) {
                if (!checkPermission(event.getPlayer(), NoLimitPermissions.BREAK(event.getBlock()))) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(mod.getPlugin().getLocale().trans("blocked.break"));
                }
            }
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            if (mod.getConfig().getBlockUse().isListed(event.getBlock())) {
                if (!checkPermission(event.getPlayer(), NoLimitPermissions.USE(event.getBlock()))) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(mod.getPlugin().getLocale().trans("blocked.place"));
                }
            }
        }
    }
    @EventHandler
    public void onAttachedBlockBreak(AttachedBlockDestroyedByPlayerEvent event) {
        if (mod.isDebug())
            mod.getLog().debug("Attached Block " + event.getBlock().getType() + " initial destroyed by player: " + event.getPlayer().getName() + " in GM " + event.getPlayer().getGameMode().toString());
        if (event.getPlayer() != null && event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            if (!checkPermission(event.getPlayer(), NoLimitPermissions.DROP)) {
                mod.getBlockSpawn().block(event.getBlock());
            }
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void onHangingBreakByPlayer(HangingBreakByPlayerBlockEvent event) {
        if (mod.isDebug())
            mod.getLog().debug("Attached Hanging " + event.getEntity().getType() + " destroyed by player block place/break: " + event.getPlayer().getName() + " in GM " + event.getPlayer().getGameMode().toString());
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            event.getDrops().clear();
        }
    }
    
    private boolean checkPermission(Player player, IDynamicPermission perm) {
        return mod.getPlugin().getPermManager().hasPermission(player, perm);
    }
    private boolean checkPermission(Player player, IAbstractPermission perm) {
        return mod.getPlugin().getPermManager().hasPermission(player, perm);
    }
}
