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

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
//import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;

public class WorldGuardIntegration {
    public static LimitedCreativeCore plugin;
    public static WorldGuardPlugin wg;

    public WorldGuardIntegration(LimitedCreativeCore pplugin) {
        plugin = pplugin;
        wg = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
    }
    

    public static final StateFlag CREATIVE_MEMBER = new StateFlag("creative-member", false);
    
    public void init() {
        
    }
}
