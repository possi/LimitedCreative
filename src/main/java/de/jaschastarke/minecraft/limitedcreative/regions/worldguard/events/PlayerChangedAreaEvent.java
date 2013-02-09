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

import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.ApplicableRegions;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.CustomRegionManager;

public class PlayerChangedAreaEvent extends PlayerNewLocationAreaEvent implements Cancellable {
    private PlayerMoveEvent event;
    private String _previous_hash;
    private boolean _cancelled = false;
    
    public PlayerChangedAreaEvent(CustomRegionManager mgr, PlayerMoveEvent moveevent) {
        super(mgr, moveevent.getPlayer(), moveevent.getTo());
        event = moveevent;
    }
    public PlayerChangedAreaEvent(CustomRegionManager mgr, PlayerMoveEvent moveevent, String previous_hash, String new_hash) {
        super(mgr, moveevent.getPlayer(), moveevent.getTo(), new_hash);
        event = moveevent;
        _previous_hash = previous_hash;
    }
    
    public boolean isTeleport() {
        return event instanceof PlayerTeleportEvent;
    }
    
    public ApplicableRegions getPreviousRegionSet() {
        return mgr.getRegionSet(event.getFrom());
    }
    
    public String getPreviousRegionHash() {
        if (_previous_hash == null)
            _previous_hash = mgr.getRegionsHash(event.getFrom());
        return _previous_hash;
    }

    public PlayerMoveEvent getMoveEvent() {
        return event;
    }
    
    public String toString() {
        return getClass().getSimpleName()+"["+getPreviousRegionHash()+" -> "+getRegionHash()+"]";
    }
    
    @Override
    public boolean isCancelled() {
        return _cancelled;
    }
    @Override
    public void setCancelled(boolean b) {
        _cancelled = b;
    }
}
