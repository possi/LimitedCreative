/*
 * Limited Creative - (Bukkit Plugin)
 * Copyright (C) 2011  Essentials Team
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

import java.io.File;
//import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;

import static de.jaschastarke.minecraft.utils.Util.copyFile;
//import static de.jaschastarke.minecraft.utils.Locale.L;

public class Configuration {
    private FileConfiguration c;
    private File file;
    public static LimitedCreativeCore plugin;
    
    public Configuration(LimitedCreativeCore pplugin) {
        plugin = pplugin;
        
        file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists())
            //plugin.saveDefaultConfig();
            copyFile(plugin.getResource("config.yml"), file);
        
        c = plugin.getConfig();
    }
    public boolean getStoreCreative() {
        return c.getBoolean("store.creative", true);
    }
    public String getInventoryFolder() {
        return c.getString("store.folder", "inventories");
    }
    public boolean getBlockPickupInCreative() {
        return c.getBoolean("limit.pickup", true);
    }
    public boolean getSignBlock() {
        return c.getBoolean("limit.sign", true);
    }
    public boolean getPermissionsEnabled() {
        return c.getBoolean("permissions.enabled", false);
    }
    public boolean getPermissionToKeepInventory() {
        return this.getPermissionsEnabled() && c.getBoolean("permissions.keepinventory", false);
    }
    
    public void setStoreCreative(boolean value) {
        this.reload();
        c.set("store.creative", value);
        this.save();
    }
    public void setBlockPickupInCreative(boolean value) {
        this.reload();
        c.set("limit.pickup", value);
        this.save();
    }
    public void setSignBlock(boolean value) {
        this.reload();
        c.set("limit.sign", value);
        this.save();
    }
    public void setPermissionsEnabled(boolean value) {
        this.reload();
        c.set("permissions.enabled", value);
        this.save();
    }
    public void setPermissionToKeepInventory(boolean value) {
        this.reload();
        if (value == true)
            this.setPermissionsEnabled(true);
        c.set("permissions.keepinventory", value);
        this.save();
    }
    protected void reload() {
        plugin.reloadConfig();
        c = plugin.getConfig();
    }
    protected void save() {
        plugin.saveConfig();
        /*try {
            c.save(file);
        } catch (IOException e) {
            plugin.logger.severe(L("exception.config.savefail"));
            e.printStackTrace();
        }*/
    }
}
