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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.Location;

final public class Util {
    public static int versionCompare(String vers1, String vers2) {
        String[] v1 = vers1.split("\\.");
        String[] v2 = vers2.split("\\.");
        int i = 0;
        while (i < v1.length && i < v2.length && v1[i].equals(v2[i])) {
            i++;
        }
        if (i < v1.length && i < v2.length) {
            int diff = new Integer(v1[i]).compareTo(new Integer(v2[i]));
            return diff < 0 ? -1 : (diff == 0 ? 0 : 1);
        }
        return v1.length < v2.length ? -1 : (v1.length == v2.length ? 0 : 1);
    }
    
    public static void copyFile(InputStream is, File to) {
        try {
            if (to.getParentFile() != null && !to.getParentFile().exists())
                to.getParentFile().mkdirs();
            OutputStream os;
                os = new FileOutputStream(to);
            byte[] buffer = new byte[512];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void copyFile(File from, File to) {
        try {
            copyFile(new FileInputStream(from), to);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static String join(String[] list, String sep, int from, int range) {
        StringBuilder result = new StringBuilder();
        for (int i = from; i >= 0 && i < from + range && i < list.length; i++) {
            if (result.length() > 0)
                result.append(sep);
            result.append(list[i]);
        }
        return result.toString();
    }
    public static String join(String[] list, int from, int range) {
        return join(list, " ", from, range);
    }
    public static String join(String[] list, int from) {
        return join(list, " ", from, list.length - from);
    }
    public static String join(String[] list) {
        return join(list, " ", 0, list.length);
    }
    public static String join(String[] list, String sep) {
        return join(list, sep, 0, list.length);
    }
    public static String toString(Location loc) {
        return "{X: "+loc.getBlockX()+", Y: "+loc.getBlockY()+", Z: "+loc.getBlockZ()+"}";
    }
    
    /**
     * Compares 2 Version Strings
     * 
     * Only Numbers are interpreted. Any non-numeric char separates sub-versions.
     * 
     * @param v1 First version String
     * @param v2 Version String to compare with
     * @return -1 when v1 < v2; 1 when v1 > v2; 0 when equal
     */
    public static int compareVersion(String v1, String v2) {
        String[] ver1 = v1.split("[^0-9]+");
        String[] ver2 = v2.split("[^0-9]+");
        
        for (int i = 0; i < Math.min(ver1.length, ver2.length); i++) {
            int diff = new Integer(ver1[i]).compareTo(new Integer(ver2[i]));
            if (diff != 0)
                return diff < 0 ? -1 : 1;
        }
        return ver1.length == ver2.length ? 0 : ver1.length < ver2.length ? -1 : 1;
        
        /*String[] vals1 = v1.split("\\.");
        String[] vals2 = v2.split("\\.");
        int i=0;
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
          i++;
        }

        if (i < vals1.length && i < vals2.length) {
            int diff = new Integer(vals1[i]).compareTo(new Integer(vals2[i]));
            return diff < 0 ? -1 : diff == 0 ? 0 : 1;
        }

        return vals1.length < vals2.length ? -1 : vals1.length == vals2.length ? 0 : 1;*/
    }
}
