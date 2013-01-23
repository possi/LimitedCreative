package de.jaschastarke.minecraft.limitedcreative.hooks;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;

import de.jaschastarke.hooking.AbstractHooker;

public class WorldTypeHooker extends AbstractHooker<WorldTypeHooker.Check> {
    public interface Check {
        GameMode get(World world);
    }
    
    public GameMode get(World world) {
        for (Check c : hooks) {
            return c.get(world);
        }
        return Bukkit.getServer().getDefaultGameMode();
    }
}
