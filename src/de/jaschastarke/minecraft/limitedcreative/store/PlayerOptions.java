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
package de.jaschastarke.minecraft.limitedcreative.store;

import java.io.File;
import java.io.IOException;

import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import de.jaschastarke.minecraft.limitedcreative.Core;

public class PlayerOptions {
    private File _store_file = new File(Core.plugin.getDataFolder(), "players.yml");
    private YamlConfiguration store = YamlConfiguration.loadConfiguration(_store_file);
    
    {
        store.options().header("DO NOT MODIFY THIS FILE");
    }
    
    public void setRegionGameMode(String player, GameMode gm) {
        if (gm == null)
            store.set(player + ".region_gamemode", null);
        else
            store.set(player + ".region_gamemode", gm.name());
        save();
    }
    public GameMode getRegionGameMode(String player) {
        if (store.contains(player + ".region_gamemode")) {
            return GameMode.valueOf(store.getString(player + ".region_gamemode"));
        } else if (store.contains(player + ".region_creative")) { // compatibility fallback
            return store.getBoolean(player + ".region_creative") ? GameMode.CREATIVE : null;
        }
        return null;
    }
    
    public GameMode getOptionalRegionGameMode(String player, String region) {
        if (store.contains(player+".region")) {
            ConfigurationSection sect = store.getConfigurationSection(player+".region");
            if (sect.contains(region)) {
                return GameMode.valueOf(sect.getString(region));
            }
        }
        return null;
    }
    public void setOptionalRegionGameMode(String player, String region, GameMode gm) {
        ConfigurationSection sect = store.contains(player+".region") ? store.getConfigurationSection(player+".region") : store.createSection(player+".region");
        String mode = gm == null ? null : gm.name();
        sect.set(region, mode);
        if (sect.getKeys(true).size() == 0)
            store.set(sect.getCurrentPath(), null);
        save();
    }

    protected void save() {
        try {
            store.save(_store_file);
        } catch (IOException e) {
            Core.plugin.logger.severe("Failed to save players.yml");
            e.printStackTrace();
        }
    }
}
