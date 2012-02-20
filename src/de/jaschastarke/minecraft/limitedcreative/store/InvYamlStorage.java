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

import org.bukkit.configuration.file.YamlConfiguration;

import de.jaschastarke.minecraft.limitedcreative.Core;
import de.jaschastarke.minecraft.limitedcreative.Inventory;
import de.jaschastarke.minecraft.limitedcreative.Inventory.Target;

public class InvYamlStorage extends InvConfStorage {
    private static final String SUFFIX = ".yml";
    
    private File dir;
    public InvYamlStorage(File file) {
        dir = file;
    }
    
    @Override
    public void load(Inventory pinv, Target target) {
        load(pinv, YamlConfiguration.loadConfiguration(getFile(pinv, target)));
    }
    
    @Override
    public void store(Inventory pinv, Target target) {
        YamlConfiguration yml = new YamlConfiguration();
        yml.options().header("DO NOT MODIFY THIS FILE");
        store(pinv, yml);
        try {
            yml.save(getFile(pinv, target));
        } catch (IOException e) {
            Core.plugin.warn("Failed to save Inventory for Player " + pinv.getPlayer().getName());
            e.printStackTrace();
        }
    }

    @Override
    public void remove(Inventory pinv, Target target) {
        getFile(pinv, target).delete();
    }

    @Override
    public boolean contains(Inventory pinv, Target target) {
        return getFile(pinv, target).exists();
    }
    
    protected File getFile(Inventory pinv, Target target) {
        if (target != default_target) {
            return new File(dir, pinv.getPlayer().getName() + "_" + target.toString().toLowerCase() + SUFFIX);
        } else {
            return new File(dir, pinv.getPlayer().getName() + SUFFIX);
        }
    }
}
