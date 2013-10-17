package de.jaschastarke.minecraft.limitedcreative.hooks;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.IllegalPluginAccessException;

import de.jaschastarke.bukkit.lib.SimpleModule;
import de.jaschastarke.minecraft.limitedcreative.Hooks;
import de.jaschastarke.minecraft.limitedcreative.LimitedCreative;
import fr.xephi.authme.api.API;
import fr.xephi.authme.events.LoginEvent;

public class AuthMe3Hooks extends SimpleModule<LimitedCreative> implements Listener {
    private Map<String, Boolean> users = new HashMap<String, Boolean>();
    private PlayerCheckHooker.Check check;
    public AuthMe3Hooks(final LimitedCreative plugin) {
        super(plugin);
        check = new PlayerCheckHooker.Check() {
            @Override
            public boolean test(Player player) {
                if (users.containsKey(player.getName())) {
                    if (plugin.isDebug()) // not nessesary, but so no string concation without debug needed
                        plugin.getLog().debug("AuthMe: "+player.getName()+": Event logged in: "+users.get(player.getName()));
                    return users.get(player.getName());
                } else {
                    boolean li = API.isAuthenticated(player);
                    if (plugin.isDebug()) // not nessesary, but so no string concation without debug needed
                        plugin.getLog().debug("AuthMe: "+player.getName()+": logged in: "+li);
                    return li;
                }
            }
        };
    }
    
    @Override
    public void onEnable() {
        try {
            super.onEnable();
        } catch (IllegalPluginAccessException e) {
            enabled = true;
            plugin.getLog().warn("AuthMe 3.0 Bug detected. Update AuthMe to a newer version as soon as avilable.");
        }
        Hooks.IsLoggedIn.register(check);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Hooks.IsLoggedIn.unregister(check);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getPlayer() == null)
            return;
        users.put(event.getPlayer().getName(), API.isAuthenticated(event.getPlayer()));
    }
    
    @EventHandler
    public void onAuthMeLogin(LoginEvent event) {
        users.put(event.getPlayer().getName(), true);
    }
}
