package de.jaschastarke.minecraft.limitedcreative.hooks;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;

import de.jaschastarke.minecraft.limitedcreative.Hooks;
import de.jaschastarke.minecraft.limitedcreative.LimitedCreative;
import multiworld.MultiWorldPlugin;
import multiworld.api.MultiWorldAPI;

public class MultiWorldHooks {
    public MultiWorldHooks(final LimitedCreative plugin) {
        Hooks.DefaultWorldGameMode.register(new WorldTypeHooker.Check() {
            @Override
            public GameMode get(World world) {
                boolean creative = getMWApi().isCreativeWorld(world.getName());
                GameMode gm = creative ? GameMode.CREATIVE : GameMode.SURVIVAL;
                plugin.getLog().debug("MultiWorld: "+world.getName()+": game mode: "+gm);
                return gm;
            }
        });
    }
    
    private static MultiWorldAPI getMWApi() {
        return ((MultiWorldPlugin) Bukkit.getServer().getPluginManager().getPlugin("MultiWorld")).getApi();
    }
}
