package de.jaschastarke.minecraft.limitedcreative.hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthPlayer;

import de.jaschastarke.minecraft.limitedcreative.Hooks;
import de.jaschastarke.minecraft.limitedcreative.LimitedCreative;

public class xAuthHooks {
    public xAuthHooks(final LimitedCreative plugin) {
        Hooks.IsLoggedIn.register(new PlayerCheckHooker.Check() {
            @Override
            public boolean test(Player player) {
                xAuthPlayer xpl = getAuth().getPlayerManager().getPlayer(player);
                boolean li = true;
                if (!xpl.isAuthenticated())
                    li = false;
                else if (xpl.isGuest())
                    li = false;
                plugin.getLog().debug("xAuth: "+player.getName()+": logged in not guest: "+li);
                return li;
            }
        });
    }
    
    private static xAuth getAuth() {
        return (xAuth) Bukkit.getServer().getPluginManager().getPlugin("xAuth");
    }
}
