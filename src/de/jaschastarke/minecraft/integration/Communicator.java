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
package de.jaschastarke.minecraft.integration;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import de.jaschastarke.minecraft.limitedcreative.Core;

public class Communicator extends AbstractCommunicator {
    public Communicator(JavaPlugin plugin) {
        super(plugin);
    }

    public boolean isLoggedIn(Player player) {
        if (isPluginEnabled("AuthMe") && !AuthMe.isLoggedInComplete(player))
            return false;
        if (isPluginEnabled("xAuth") && !xAuth.isLoggedInNotGuest(player))
            return false;
        return true;
    }
    
    public boolean isCreative(World world) {
        boolean creative = Bukkit.getServer().getDefaultGameMode() == GameMode.CREATIVE;
        if (isPluginEnabled("Multiverse-Core"))
            creative = MultiVerse.isCreative(world);
        Core.debug("com: "+world.getName()+": is creative: "+creative);
        return creative;
    }
    public GameMode getDefaultGameMode(World world) {
        GameMode def = Bukkit.getServer().getDefaultGameMode();
        if (isPluginEnabled("Multiverse-Core"))
            def = MultiVerse.getGameMode(world);
        Core.debug("com: "+world.getName()+": game mode: "+def);
        return def;
    }
}
