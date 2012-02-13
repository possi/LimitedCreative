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

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import de.jaschastarke.minecraft.limitedcreative.LimitedCreativeCore;

public class Locale {
    protected YamlConfiguration lang;
    private YamlConfiguration fallback_lang;
    private static Locale inst = null;
    private JavaPlugin plugin;
    private final static String DEFAULT_LANG = "en_US";
    
    public Locale(JavaPlugin plugin) {
        this(plugin, null);
    }
    
    public Locale(JavaPlugin plugin, String lang) {
        if (inst == null)
            inst = this;
        this.plugin = plugin;
        if (lang == null)
            lang = java.util.Locale.getDefault().toString();
        
        String fn = getFilename(lang);
        
        LimitedCreativeCore.debug("Using Locale: " + lang);
        File localefile = new File(plugin.getDataFolder(), fn);
        if (localefile.exists())
            this.lang = YamlConfiguration.loadConfiguration(localefile);
        else if (plugin.getResource(fn) != null)
            this.lang = YamlConfiguration.loadConfiguration(plugin.getResource(fn));
    }
    private String getFilename(String locale) {
        return "lang/"+locale+".yml";
    }
    private YamlConfiguration getLang(String msg) {
        if (lang != null && lang.contains(msg)) {
            return lang;
        } else {
            if (fallback_lang == null)
                fallback_lang = YamlConfiguration.loadConfiguration(plugin.getResource(getFilename(DEFAULT_LANG)));
            return fallback_lang;
        }
    }
    public String get(String msg, Object... objects) {
        YamlConfiguration lang = getLang(msg);
        if (lang.contains(msg)) {
            if (lang.isList(msg)) {
                List<String> list = lang.getStringList(msg);
                String[] lines = new String[list.size()];
                list.toArray(lines);
                msg = Util.join(lines, "\n");
            } else {
                msg = lang.getString(msg);
            }
        }
        if (objects.length > 0)
            msg = MessageFormat.format(msg, objects);
        return msg.replaceAll("&([0-9a-f])", "\u00A7$1");
    }

    /**
     * Static localization-access only works for first locale instance. if used by another plugin, you need to
     * access the Locale-Instance get-Method
     */
    public static String L(String msg, Object... objects) {
        return (inst != null) ? inst.get(msg, objects) : msg;
    }
    public static void unload() {
        inst = null;
    }
}
