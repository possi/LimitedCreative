package de.jaschastarke.minecraft.limitedcreative.regions;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import de.jaschastarke.minecraft.limitedcreative.Hooks;
import de.jaschastarke.minecraft.limitedcreative.ModRegions;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.ApplicableRegions;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.FlagList;

public class WorldGuardIntegration implements IWorldGuardIntegration {
    public static final String PLUGIN_NAME = "WorldGuard";
    private ModRegions mod;
    
    public WorldGuardIntegration(ModRegions mod) {
        this.mod = mod;
    }

    @Override
    public void initFlagList() {
        FlagList.addFlags(Flags.getList());
    }

    @Override
    
    public boolean isRegionOptional(Player player, GameMode tgm) {
        ApplicableRegions rs = mod.getRegionManager().getRegionSet(player.getLocation());
        if (rs.allows(Flags.GAMEMODE_OPTIONAL)) {
            if ((tgm == rs.getFlag(Flags.GAMEMODE, player)) || (tgm == Hooks.DefaultWorldGameMode.get(player.getWorld())))
                return true;
        }
        return false;
    }

}
