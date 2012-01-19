package de.jaschastarke.minecraft.regions;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.jaschastarke.minecraft.limitedcreative.LCPlayer;
import de.jaschastarke.minecraft.limitedcreative.WorldGuardIntegration;

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
    
    public boolean allows(StateFlag flag, LCPlayer player) {
        extendRegionFlags();
        boolean r = regions.allows(flag, WorldGuardIntegration.wg.wrapPlayer(player.getRaw()));
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
    }
    @SuppressWarnings("unchecked")
    private <T extends Flag<V>, V> void contractRegionFlags() {
        for (ProtectedRegion pr : regions) {
            for (FlagValue data : mgr.region(pr).getFlags()) {
                T flag = (T) data.getFlag();
                pr.setFlag(flag, null);
            }
        }
    }
    
}
