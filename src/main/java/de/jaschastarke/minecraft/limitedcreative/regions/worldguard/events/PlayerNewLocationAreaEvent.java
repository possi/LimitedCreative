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

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.ApplicableRegions;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.CustomRegionManager;


public class PlayerNewLocationAreaEvent extends PlayerAreaEvent {
    private Location location;
    protected CustomRegionManager mgr;
    private Player player;
    private String _hash;
    
    public PlayerNewLocationAreaEvent(CustomRegionManager mgr, Player player, Location new_location) {
        this.mgr = mgr;
        this.player = player;
        location = new_location;
    }
    public PlayerNewLocationAreaEvent(CustomRegionManager mgr, Player player, Location new_location, String new_hash) {
        this(mgr, player, new_location);
        _hash = new_hash;
    }
    
    @Override
    public String getRegionHash() {
        if (_hash == null)
            _hash = mgr.getRegionsHash(location);
        return _hash;
    }
    @Override
    public ApplicableRegions getRegionSet() {
        return mgr.getRegionSet(location);
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public String toString() {
        return getClass().getSimpleName()+"["+getRegionHash()+"]";
    }
    
    private static final HandlerList handlers = new HandlerList();
    
    public HandlerList getHandlers() {
        return handlers;
    }
     
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
