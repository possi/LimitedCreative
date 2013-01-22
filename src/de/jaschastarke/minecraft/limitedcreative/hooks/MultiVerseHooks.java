package de.jaschastarke.minecraft.limitedcreative.hooks;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;

import de.jaschastarke.hooking.BooleanHooker;
import de.jaschastarke.minecraft.limitedcreative.Hooks;
import de.jaschastarke.minecraft.limitedcreative.LimitedCreative;

public class MultiVerseHooks {
    public MultiVerseHooks(final LimitedCreative plugin) {
        Hooks.IsMultiVerse.register(new BooleanHooker.Check() {
            @Override
            public boolean test() {
                return true;
            }
        });
        Hooks.DefaultWorldGameMode.register(new WorldTypeHooker.Check() {
            @Override
            public GameMode get(World world) {
                MultiverseWorld mvWorld = getMV().getMVWorldManager().getMVWorld(world);
                if (mvWorld == null)
                    return null;
                GameMode gm = mvWorld.getGameMode();
                plugin.getLog().debug("Multiverse: "+world.getName()+": game mode: "+gm);
                return gm;
            }
        });
    }
    
    private static MultiverseCore getMV() {
        return (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
    }
}
