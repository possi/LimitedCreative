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
package de.jaschastarke.minecraft.limitedcreative.regions.worldguard.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.ApplicableRegions;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.CustomRegionManager;

public class PlayerUpdateAreaEvent extends PlayerAreaEvent {
    private Player player;
    private String hash;
    protected CustomRegionManager mgr;
    
    public PlayerUpdateAreaEvent(CustomRegionManager mgr, Player player, String hash) {
        this.mgr = mgr;
        this.player = player;
        this.hash = hash;
    }
    @Override
    public String getRegionHash() {
        return hash;
    }

    @Override
    public ApplicableRegions getRegionSet() {
        return mgr.getRegionSet(getPlayer().getLocation());
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    private static final HandlerList handlers = new HandlerList();
    
    public HandlerList getHandlers() {
        return handlers;
    }
     
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
