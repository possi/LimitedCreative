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
package de.jaschastarke.minecraft.worldguard;

import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.jaschastarke.minecraft.limitedcreative.regions.WorldGuardIntegration;

public class ApplicableRegions {
    private ApplicableRegionSet regions;
    private CRegionManager.CWorld mgr;

    public ApplicableRegions(ApplicableRegionSet regions, CRegionManager.CWorld rm) {
        this.regions = regions;
        this.mgr = rm;
    }
    

    public boolean allows(StateFlag flag) {
        extendRegionFlags();
        boolean r = regions.allows(flag);
        contractRegionFlags();
        return r;
    }
    
    public boolean allows(StateFlag flag, Player player) {
        extendRegionFlags();
        boolean r = regions.allows(flag, WorldGuardIntegration.wg.wrapPlayer(player));
        contractRegionFlags();
        return r;
    }

    @SuppressWarnings("unchecked")
    private <T extends Flag<V>, V> void extendRegionFlags() {
        for (ProtectedRegion pr : regions) {
            for (FlagValue data : mgr.region(pr).getFlags()) {
                T flag = (T) data.getFlag();
                V value = (V) data.getValue();
                pr.setFlag(flag, value);
            }
        }
        if (mgr.getGlobalRegion() != null) {
            for (FlagValue data : mgr.region(mgr.getGlobalRegion()).getFlags()) {
                T flag = (T) data.getFlag();
                V value = (V) data.getValue();
                mgr.getGlobalRegion().setFlag(flag, value);
            }
        }
    }
    @SuppressWarnings("unchecked")
    private <T extends Flag<V>, V> void contractRegionFlags() {
        for (ProtectedRegion pr : regions) {
            for (FlagValue data : mgr.region(pr).getFlags()) {
                T flag = (T) data.getFlag();
                pr.setFlag(flag, null);
            }
        }
        if (mgr.getGlobalRegion() != null) {
            for (FlagValue data : mgr.region(mgr.getGlobalRegion()).getFlags()) {
                T flag = (T) data.getFlag();
                mgr.getGlobalRegion().setFlag(flag, null);
            }
        }
    }
    
}
