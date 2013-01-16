package de.jaschastarke.minecraft.limitedcreative.hooks;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;

import de.jaschastarke.hooking.AbstractHooker;

public class WorldTypeHooker extends AbstractHooker<WorldTypeHooker.Check> {
    public interface Check {
        GameMode test(World world);
    }
    
    public GameMode test(World world) {
        for (Check c : hooks) {
            return c.test(world);
        }
        return Bukkit.getServer().getDefaultGameMode();
    }
}
