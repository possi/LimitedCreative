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
import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import static de.jaschastarke.minecraft.utils.Util.versionCompare;
import de.jaschastarke.minecraft.utils.Locale;


public class LimitedCreativeCore extends JavaPlugin {
    public final Logger logger = Logger.getLogger("Minecraft");
    public Configuration config;
    public WorldGuardIntegration worldguard;
    public static LimitedCreativeCore plugin;
    
    public static boolean serializeFallBack = false;

    @Override
    public void onDisable() {
        Locale.unload();
        logger.info("["+this.getDescription().getName()+"] cleanly unloaded.");
    }

    @Override
    public void onEnable() {
        plugin = this;
        config = new Configuration(this);
        serializeFallBack = versionCompare(getServer().getBukkitVersion().replaceAll("-.*$", ""), "1.1") < 0;
        
        new Locale(this);
        
        Listener.register(this);
        Commands.register(this);
        
        try {
            Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin", false, null);
            worldguard = new WorldGuardIntegration(this);
            worldguard.init();
        } catch (ClassNotFoundException e) {}
        
        PluginDescriptionFile df = this.getDescription();
        logger.info("["+df.getName() + " v" + df.getVersion() + "] done loading.");
    }
}
