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
package de.jaschastarke.minecraft.limitedcreative.regions;

import java.io.File;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import de.jaschastarke.minecraft.limitedcreative.LimitedCreativeCore;
import de.jaschastarke.minecraft.worldguard.CRegionManager;
import de.jaschastarke.minecraft.worldguard.FlagList;

public class WorldGuardIntegration {
    public static LimitedCreativeCore plugin;
    public static WorldGuardPlugin wg;
    private CRegionManager rm;

    public WorldGuardIntegration(LimitedCreativeCore plugin) {
        WorldGuardIntegration.plugin = plugin;
        wg = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        
        rm = new CRegionManager(new File(plugin.getDataFolder(), "regions.yml"));
        
        FlagList.addFlag(Flags.CREATIVE);
        FlagList.addFlag(Flags.CREATIVE_GROUP);
        FlagList.addFlag(Flags.SPAWNDROPS);
        
        plugin.getServer().getPluginManager().registerEvents(new RegionListener(this), plugin);
    }
    
    public CRegionManager getRegionManager() {
        return rm;
    }
}
