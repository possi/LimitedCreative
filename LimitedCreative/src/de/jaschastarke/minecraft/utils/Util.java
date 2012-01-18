package de.jaschastarke.minecraft.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
}
