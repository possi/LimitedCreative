package de.jaschastarke.minecraft.limitedcreative.regions;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public interface IWorldGuardIntegration {
    public void initFlagList();
    public boolean isRegionOptional(Player player, GameMode tgm);
}
