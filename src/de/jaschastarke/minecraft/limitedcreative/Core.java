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

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginDescriptionFile;

import de.jaschastarke.bukkit.lib.locale.PluginLang;
import de.jaschastarke.minecraft.integration.Communicator;
import de.jaschastarke.minecraft.limitedcreative.cmdblock.CommandBlocker;
import de.jaschastarke.minecraft.limitedcreative.limits.LimitListener;
import de.jaschastarke.minecraft.limitedcreative.listeners.MainListener;
import de.jaschastarke.minecraft.limitedcreative.regions.WorldGuardIntegration;
import de.jaschastarke.minecraft.utils.Permissions;

public class Core extends de.jaschastarke.bukkit.lib.Core {
    public Configuration config;
    public Permissions perm;
    public WorldGuardIntegration worldguard;
    public Communicator com;
    public static Core plugin;
    public NoBlockItemSpawn spawnblock;
    public CommandBlocker cmdblock;

    @Override
    public void onDisable() {
        plugin.getServer().getScheduler().cancelTasks(this);
        if (worldguard != null)
            worldguard.unload();
        
        plugin = null;
        worldguard = null;
        config = null;
        spawnblock = null;
        com = null;
        cmdblock = null;
    }

    @Override
    public void onEnable() {
        plugin = this;
        config = new Configuration(this);
        perm = new Permissions(this);
        com = new Communicator(this);
        
        new PluginLang(this, config.getLocale());

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
        
        // 4th Feature: Command Blocker
        if (config.getCommandBlockerEnabled())
            cmdblock = new CommandBlocker(this);
        
        debug("Store: " + config.getStoreEnabled());
        debug("Limit: " + config.getLimitEnabled());
        debug("Region: " + (worldguard != null));
        debug("CmdBlock: " + config.getCommandBlockerEnabled());
        
        Commands.register(this);
        
        /*plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                Players.cleanUp();
            }
        }, Players.CLEANUP_TIMEOUT / 50L, Players.CLEANUP_TIMEOUT / 50L); // 50 = 1000ms / 20ticks*/
        
        PluginDescriptionFile df = this.getDescription();
        if (worldguard != null)
            getLog().info("["+df.getName() + " v" + df.getVersion() + "] "+L("basic.loaded.worldguard"));
        else
            getLog().info("["+df.getName() + " v" + df.getVersion() + "] "+L("basic.loaded.no_worldguard"));
    }
    
    public void reload() {
        getServer().getScheduler().cancelTasks(this);
        getServer().getServicesManager().unregisterAll(this);
        HandlerList.unregisterAll(this);
        setEnabled(false);
        setEnabled(true);
    }
    public void info(String s) {
        getLog().info("["+this.getDescription().getName()+"] " + s);
    }
    public void warn(String s) {
        getLog().warning("["+this.getDescription().getName()+"] " + s);
    }
    public void error(String s) {
        getLog().severe("["+this.getDescription().getName()+"] " + s);
    }
    /**
     * Static localization-access only works for first locale instance. if used by another plugin, you need to
     * access the Locale-Instance get-Method
     */
    public static String L(String msg, Object... objects) {
        return (plugin.getTranslation() != null) ? plugin.getTranslation().get(msg, objects) : msg;
    }

    public static void debug(String s) {
        if (isDebug())
            plugin.info("DEBUG: " + s);
    }
    public static boolean isDebug() {
        return plugin.config.getDebug();
    }
}
