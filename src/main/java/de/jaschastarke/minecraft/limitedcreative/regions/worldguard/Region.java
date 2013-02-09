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

import java.util.Iterator;
import java.util.List;

import org.bukkit.World;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.CustomRegionManager.CWorld;

public class Region {
    private ProtectedRegion region;
    private CWorld mgr;
    private List<FlagValue> flags = null;
    
    protected Region(CWorld w, ProtectedRegion reg) {
        mgr = w;
        region = reg;
    }
    
    public void removeFlag(Flag<?> flag) {
        if (flags != null) {
            Iterator<FlagValue> i = flags.iterator();
            while (i.hasNext()) {
                if (i.next().getFlag() == flag)
                    i.remove();
            }
        }
        mgr.storeFlag(this, flag, null);
    }
    
    public void setFlag(Flag<?> flag, Object value) {
        if (value == null) {
            removeFlag(flag);
        } else {
            if (flags != null)
                flags.add(new FlagValue(flag, value));
            mgr.storeFlag(this, flag, value);
        }
    }
    public ProtectedRegion getProtectedRegion() {
        return region;
    }
    public World getWorld() {
        return mgr.getWorld();
    }
    public List<FlagValue> getFlags() {
        if (flags == null) {
            flags = mgr.getFlags(this);
        }
        return flags;
    }
}
