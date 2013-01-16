package de.jaschastarke.minecraft.limitedcreative.hooks;

import org.bukkit.entity.Player;

import uk.org.whoami.authme.cache.auth.PlayerCache;

import de.jaschastarke.minecraft.limitedcreative.Hooks;
import de.jaschastarke.minecraft.limitedcreative.LimitedCreative;

public class AuthMeHooks {
    public AuthMeHooks(final LimitedCreative plugin) {
        Hooks.IsLoggedIn.register(new PlayerCheckHooker.Check() {
            @Override
            public boolean test(Player player) {
                boolean li = PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase());
                if (plugin.isDebug()) // not nessesary, but so no string concation without debug needed
                    plugin.getLog().debug("AuthMe: "+player.getName()+": logged in: "+li);
                return li;
            }
        });
    }
}
