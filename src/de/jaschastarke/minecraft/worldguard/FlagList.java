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
package de.jaschastarke.minecraft.worldguard;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.sk89q.worldguard.protection.flags.Flag;

public class FlagList {
    protected static List<Flag<?>> list = new ArrayList<Flag<?>>();
    
    public static List<Flag<?>> getFlags() {
        return list;
    }
    public static void addFlag(Flag<?> flag) {
        list.add(flag);
    }
    public static Flag<?> getFlag(String flag) {
        for (Flag <?> f : list) {
            if (f.getName().replace("-", "").equalsIgnoreCase(flag.replace("-", ""))) {
                return f;
            }
        }
        return null;
    }
    public static List<Flag<?>> getAvailableFlags(CommandSender sender) {
        List<Flag<?>> result = new ArrayList<Flag<?>>();
        for (Flag <?> f : list) {
            if (!(f instanceof IRestrictedFlag) || ((IRestrictedFlag) f).isAllowed(sender)) {
                result.add(f);
            }
        }
        return result;
    }
    public static String getStringListAvailableFlags(CommandSender sender) {
        String result = "";
        for (Flag <?> f : getAvailableFlags(sender)) {
            if (result.length() > 0)
                result += ", ";
            result += f.getName();
        }
        return result;
    }
}
