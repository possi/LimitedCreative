package de.jaschastarke.minecraft.limitedcreative.regions;

import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;

public final class Flags {
    public static final StateFlag CREATIVE = new StateFlag("creative", false);
    public static final RegionGroupFlag CREATIVE_GROUP = new RegionGroupFlag("creative-group", RegionGroupFlag.RegionGroup.MEMBERS);
    static {
        CREATIVE.setGroupFlag(CREATIVE_GROUP);
    }
}
