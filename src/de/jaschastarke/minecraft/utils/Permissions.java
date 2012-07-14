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
package de.jaschastarke.minecraft.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import de.jaschastarke.minecraft.limitedcreative.Core;
import de.jaschastarke.minecraft.worldedit.PermissionsBridge;

public class Permissions {
    protected JavaPlugin plugin = null;
    protected PermissionsBridge pif = null;
    
    public Permissions(JavaPlugin plugin) {
        this.plugin = plugin;
        try {
        	if (Core.plugin.config.getWEPIFEnabled()) {
	            // because worldedit may be not loaded as plugin, just as library, we check that way
	            Class.forName("com.sk89q.wepif.PermissionsResolverManager", false, plugin.getClass().getClassLoader());
	            pif = new PermissionsBridge(plugin);
        	}
        } catch (ClassNotFoundException e) {}
    }
    
    public boolean hasPermission(Player player, String permission) {
        boolean ret = false;
        if (pif != null) {
            ret = pif.hasPermission(player, permission);
        } else {
            // bukkit permission fallback
            ret = player.hasPermission(permission);
        }
        debug(player, permission, ret);
        return ret;
    }

    public boolean hasPermission(CommandSender player, IPermission permission) {
        return hasPermission(player, permission.toString());
    }
    
    public boolean hasPermission(CommandSender sender, String permission) {
        if (sender instanceof Player) {
            return hasPermission((Player) sender, permission);
        } else {
            debug(sender, permission, true);
            return true;
        }
    }
    
    private void debug(CommandSender player, String permission, boolean result) {
        if (plugin instanceof Core && ((Core) plugin).config.getDebug())
            Core.debug("hasPermission: " + player.getName() + " - " + permission + " - " + result);
    }
}
