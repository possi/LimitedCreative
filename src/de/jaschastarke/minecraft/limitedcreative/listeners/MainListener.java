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

import org.bukkit.GameMode;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import de.jaschastarke.minecraft.limitedcreative.LCPlayer;
import de.jaschastarke.minecraft.limitedcreative.LimitedCreativeCore;

public class MainListener implements Listener {
    private LimitedCreativeCore plugin;
    public MainListener(LimitedCreativeCore plugin) {
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
        LimitedCreativeCore.debug("onPlayerGameModeChange: "+event.getPlayer().getName());
        LimitedCreativeCore.debug("Current GameMode: "+event.getPlayer().getGameMode());
        LimitedCreativeCore.debug("New GameMode: "+event.getNewGameMode());
        LimitedCreativeCore.debug("isCancelled: "+event.isCancelled());
        if (event.getNewGameMode() == GameMode.CREATIVE) {
            if (!LCPlayer.get(event.getPlayer()).onSetCreative())
                event.setCancelled(true);
        } else if (event.getNewGameMode() == GameMode.SURVIVAL) {
            if (!LCPlayer.get(event.getPlayer()).onSetSurvival())
                event.setCancelled(true);
        }
    }
    
    /**
     * Also needed if WorldGuard-Feature is enabled, so can not moved to optional Listener "Limit".
     */
    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (event.getEntity() instanceof Item) {
            if (plugin.spawnblock.isBlocked(event.getLocation().getBlock().getLocation(), ((Item) event.getEntity()).getItemStack().getType())) {
                event.setCancelled(true);
            }
        }
    }
    

    /*
    public static class VehicleListen extends VehicleListener {
        @EventHandler
        public void onVehicleDestroy(VehicleDestroyEvent event) {
            if (event.isCancelled())
                return;
            if (event.getAttacker() instanceof Player) {
                Player player = (Player) event.getAttacker();
                if (player.getGameMode() == GameMode.CREATIVE) {
                    if (plugin.config.getPermissionsEnabled() && player.hasPermission("limitedcreative.nolimit.drop"))
                        return;
                    plugin.logger.info("Vehicle destroy: "+event.getVehicle() + " - "+event.getVehicle().getEntityId());
                }
            }
        }
        
        private void register() {
            if (plugin.config.getLimitEnabled()) {
                pm.registerEvent(Event.Type.VEHICLE_DESTROY, this, Priority.Normal, plugin);
            }
        }
    }*/
}
