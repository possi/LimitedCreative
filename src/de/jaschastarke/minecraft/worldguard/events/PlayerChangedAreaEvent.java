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
package de.jaschastarke.minecraft.worldguard.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.jaschastarke.minecraft.worldguard.ApplicableRegions;
import de.jaschastarke.minecraft.worldguard.Interface;

@SuppressWarnings("serial")
public class PlayerChangedAreaEvent extends PlayerAreaEvent implements Cancellable {
    private PlayerMoveEvent event;
    private String _previous_hash;
    private String _hash;
    private boolean _cancelled = false;
    
    public PlayerChangedAreaEvent(PlayerMoveEvent moveevent) {
        event = moveevent;
    }
    public PlayerChangedAreaEvent(PlayerMoveEvent moveevent, String previous_hash, String new_hash) {
        event = moveevent;
        _previous_hash = previous_hash;
        _hash = new_hash;
    }
    
    @Override
    public String getRegionHash() {
        if (_hash == null)
            _hash = Interface.getInstance().getRegionManager().getRegionsHash(event.getTo());
        return _hash;
    }
    @Override
    public ApplicableRegions getRegionSet() {
        return Interface.getInstance().getRegionManager().getRegionSet(event.getTo());
    }
    
    public Player getPlayer() {
        return event.getPlayer();
    }
    
    public boolean isTeleport() {
        return event instanceof PlayerTeleportEvent;
    }
    
    public ApplicableRegions getPreviousRegionSet() {
        return Interface.getInstance().getRegionManager().getRegionSet(event.getFrom());
    }
    
    public String getPreviousRegionHash() {
        if (_previous_hash == null)
            _previous_hash = Interface.getInstance().getRegionManager().getRegionsHash(event.getFrom());
        return _previous_hash;
    }

    public PlayerMoveEvent getMoveEvent() {
        return event;
    }
    
    public String toString() {
        return getClass().getSimpleName()+"["+getPreviousRegionHash()+" -> "+getRegionHash()+"]";
    }
    
    private static final HandlerList handlers = new HandlerList();
    
    public HandlerList getHandlers() {
        return handlers;
    }
     
    public static HandlerList getHandlerList() {
        return handlers;
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
