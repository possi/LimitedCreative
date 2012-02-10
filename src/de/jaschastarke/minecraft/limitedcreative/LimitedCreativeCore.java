/*
 * Limited Creative - (Bukkit Plugin)
 * Copyright (C) 2012 jascha@ja-s.de
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.jaschastarke.minecraft.limitedcreative;
import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import static de.jaschastarke.minecraft.utils.Locale.L;
import de.jaschastarke.minecraft.integration.Communicator;
import de.jaschastarke.minecraft.limitedcreative.listeners.LimitListener;
import de.jaschastarke.minecraft.limitedcreative.listeners.MainListener;
import de.jaschastarke.minecraft.limitedcreative.regions.WorldGuardIntegration;
import de.jaschastarke.minecraft.utils.Locale;
import de.jaschastarke.minecraft.utils.Permissions;


public class LimitedCreativeCore extends JavaPlugin {
    public final Logger logger = Logger.getLogger("Minecraft");
    public Configuration config;
    public Permissions perm;
    public WorldGuardIntegration worldguard;
    public Communicator com;
    public static LimitedCreativeCore plugin;
    public NoBlockItemSpawn spawnblock;

    @Override
    public void onDisable() {
        plugin.getServer().getScheduler().cancelTasks(this);
        plugin = null;
        worldguard = null;
        config = null;
        spawnblock = null;
        com = null;
        try {
            Locale.unload();
        } catch (NoClassDefFoundError e) {} // prevent unload issue
    }

    @Override
    public void onEnable() {
        plugin = this;
        config = new Configuration(this);
        perm = new Permissions(this);
        com = new Communicator(this);
        
        new Locale(this);

        spawnblock = new NoBlockItemSpawn();
        
        // 1st Feature: Separated Inventories Storage
        if (config.getStoreEnabled() && getServer().getPluginManager().isPluginEnabled("MultiInv")) {
            warn(L("basic.conflict", "MultiInv", L("basic.feature.store")));
            config.setTempStoreEnabled(false);
        }
        getServer().getPluginManager().registerEvents(new MainListener(this), this);
        
        // 2nd Feature: Creative Limitations (Restrictions)
        if (config.getLimitEnabled())
            getServer().getPluginManager().registerEvents(new LimitListener(this), this);
        
        // 3rd Feature: WorldGuard Region-Support
        if (config.getRegionEnabled() && getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            worldguard = new WorldGuardIntegration(this);
        } else if(config.getRegionEnabled()) {
            warn(L("basic.warning.worldguard_not_found", L("basic.feature.region")));
        }
        
        debug("Store: " + config.getStoreEnabled());
        debug("Limit: " + config.getLimitEnabled());
        debug("Region: " + (worldguard != null));
        
        Commands.register(this);
        
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                LCPlayer.cleanUp();
            }
        }, LCPlayer.CLEANUP_TIMEOUT / 50L, LCPlayer.CLEANUP_TIMEOUT / 50L); // 50 = 1000ms / 20ticks
        
        PluginDescriptionFile df = this.getDescription();
        if (worldguard != null)
            logger.info("["+df.getName() + " v" + df.getVersion() + "] "+L("basic.loaded.worldguard"));
        else
            logger.info("["+df.getName() + " v" + df.getVersion() + "] "+L("basic.loaded.no_worldguard"));
    }
    
    public void info(String s) {
        logger.info("["+this.getDescription().getName()+"] " + s);
    }
    public void warn(String s) {
        logger.warning("["+this.getDescription().getName()+"] " + s);
    }
    public static void debug(String s) {
        if (plugin.config.getDebug())
            plugin.info("DEBUG: " + s);
    }
}
