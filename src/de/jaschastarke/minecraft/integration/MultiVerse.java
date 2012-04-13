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

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;

import de.jaschastarke.minecraft.limitedcreative.Core;

public class MultiVerse implements CommunicationBridge {
    public static GameMode getGameMode(World world) {
        MultiverseWorld mvWorld = getMV().getMVWorldManager().getMVWorld(world);
        if (mvWorld == null)
            return null;
        GameMode gm = mvWorld.getGameMode();
        Core.debug("Multiverse: "+world.getName()+": game mode: "+gm);
        return gm;
    }

    private static MultiverseCore getMV() {
        return (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
    }
}
