package de.jaschastarke.minecraft.limitedcreative.regions;

import java.util.Arrays;
import java.util.List;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;

public final class Flags {
    public static final StateFlag SPAWNDROPS = new StateFlag("spawndrops", true);
    public static final StateFlag GAMEMODE_OPTIONAL = new StateFlag("gamemode-optional", false);
    public static final GameModeFlag GAMEMODE = new GameModeFlag("gamemode", RegionGroup.MEMBERS);

    public static List<Flag<?>> getList() {
        return Arrays.asList(new Flag<?>[]{
            SPAWNDROPS,
            GAMEMODE,
            GAMEMODE_OPTIONAL,
        });
    }
}
