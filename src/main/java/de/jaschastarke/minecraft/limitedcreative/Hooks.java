package de.jaschastarke.minecraft.limitedcreative;

import org.bukkit.Bukkit;

import de.jaschastarke.hooking.BooleanHooker;
import de.jaschastarke.hooking.GetHooker;
import de.jaschastarke.minecraft.limitedcreative.hooks.MultiVerseHooks;
import de.jaschastarke.minecraft.limitedcreative.hooks.PlayerCheckHooker;
import de.jaschastarke.minecraft.limitedcreative.hooks.WorldTypeHooker;
import de.jaschastarke.minecraft.limitedcreative.hooks.xAuthHooks;

public final class Hooks {
    public static PlayerCheckHooker IsLoggedIn = new PlayerCheckHooker(true);
    public static WorldTypeHooker DefaultWorldGameMode = new WorldTypeHooker();
    public static BooleanHooker IsMultiVerse = new BooleanHooker(false);
    public static GetHooker<String> InventoryIncompatible = new GetHooker<String>();

    public static boolean isPluginEnabled(String pluginName) {
        return Bukkit.getServer().getPluginManager().isPluginEnabled(pluginName);
    }
    
    @SuppressWarnings("deprecation")
    public static void inizializeHooks(LimitedCreative plugin) {
        IsLoggedIn.clearHooks();
        DefaultWorldGameMode.clearHooks();
        IsMultiVerse.clearHooks();
        InventoryIncompatible.clearHooks();
        
        if (isAuthMePresent()) {
            new de.jaschastarke.minecraft.limitedcreative.hooks.AuthMeHooks(plugin);
        }
        if (isXAuth20Present()) {
            new xAuthHooks(plugin);
        }
        if (isPluginEnabled("Multiverse-Core")) {
            new MultiVerseHooks(plugin);
        }/* else if (isPluginEnabled("MultiWorld")) { // MultiWord suckz, the Creative-World-Setting doesn't affect anything
            new MultiWorldHooks(plugin);
        }*/
        
        InventoryIncompatible.register(new GetHooker.Check<String>() {
            @Override
            public String test() {
                if (isPluginEnabled("MultiInv"))
                    return "MultiInv";
                if (isPluginEnabled("Multiverse-Inventories"))
                    return "Multiverse-Inventories";
                return null;
            }
        });
    }
    
    public static boolean isAuthMePresent() {
        if (isPluginEnabled("AuthMe")) {
            try {
                return Class.forName("uk.org.whoami.authme.api.API") != null;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
        return false;
    }
    
    public static boolean isXAuth20Present() {
        if (isPluginEnabled("xAuth")) {
            try {
                return Class.forName("com.cypherx.xauth.xAuth") != null;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
        return false;
    }
}
