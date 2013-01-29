package de.jaschastarke.bukkit.tools.stats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.bukkit.Server;

public final class StatsUtils {
    private static final String ENCODING = "UTF-8";
    private StatsUtils() {
    }
    
    public static String getIP(final Server server) {
        String ip = server.getIp();
        if (ip.isEmpty()) {
            try {
                URL getip = new URL("http://checkip.amazonaws.com/");
                BufferedReader in = new BufferedReader(new InputStreamReader(getip.openStream()));
                ip = in.readLine();
                in.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ip;
    }
    
    public static String enc(final String s) {
        try {
            return URLEncoder.encode(s, ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }
    
    public static URL buildRequest(final URL url, final String[][] arguments) {
        StringBuilder u = new StringBuilder(url.toString());
        for (int i = 0; i < arguments.length; i++) {
            u.append(i == 0 ? "?" : "&");
            u.append(arguments[i][0]);
            u.append("=");
            u.append(enc(arguments[i][1]));
        }
        try {
            return new URL(u.toString());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Arguments couldn't build to a new URL");
        }
    }
}
