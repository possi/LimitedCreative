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
package de.jaschastarke.minecraft.worldguard;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class Interface {
    //private JavaPlugin plugin;
    private CRegionManager mgr;
    private WorldGuardPlugin wg;
    private static Interface _instance = null;
    
    public Interface(JavaPlugin plugin) {
        if (_instance != null)
            throw new RuntimeException("The Interface is Singleton!");
        //this.plugin = plugin;
        _instance = this;
        
        wg = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");

        
        mgr = new CRegionManager(new File(plugin.getDataFolder(), "regions.yml"));
        plugin.getCommand("/region").setExecutor(new CCommand(plugin, mgr, wg));
        plugin.getServer().getPluginManager().registerEvents(new CListener(this), plugin);
    }
    public static Interface getInstance() {
        return _instance;
    }

    public void register(Integration integration) {
        FlagList.addFlags(integration.getFlags());
    }
    public WorldGuardPlugin getWorldGuard() {
        return wg;
    }
    public CRegionManager getRegionManager() {
        return mgr;
    }
}
