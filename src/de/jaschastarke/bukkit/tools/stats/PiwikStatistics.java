package de.jaschastarke.bukkit.tools.stats;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Use the piwik-Proxy https://gist.github.com/4664325 to track the online-count as seperate requests to get a propper
 * graph of usage.
 * 
 * Settings as Properties:
 *  - piwik_url : required; URL to the piwik.php or the proxy file (see above)
 *  - piwik_site_id : required; The Site-ID of the Piwik-Website to use for tracking
 *  - stats_interval : optional; seconds between tracking online count (defaults to 300 for 5 min)
 */
public class PiwikStatistics implements IStatistics {
    private static final int TICKS_PER_SECOND = 20;
    private static final long DEFAULT_WAIT = 6000L; // 6000 ticks or 300 seconds or 5 minutes
    private static final int MAX_CVAR_SIZE = 200;
    private static final int APIV = 1;
    private URL apiUrl;
    private int idSite;
    private Plugin plugin;
    private String pluginname;
    private String version;
    private String server;
    private String serverid = getUniqueID();
    private String servername;
    private String servermotd;
    private long wait = DEFAULT_WAIT;
    
    private static final String PIWIK_FIELD_CVAR = "cvar";
    
    /**
     * Single call instantiate
     * 
     * Also calls .register, 
     */
    public PiwikStatistics(final JavaPlugin plugin) {
        Properties settings = new Properties();
        try {
            settings.load(plugin.getClass().getClassLoader().getResourceAsStream("settings.properties"));
            init(settings);
            register(plugin);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Single call instantiate
     * 
     * Also calls .register, 
     */
    public PiwikStatistics(final JavaPlugin plugin, final InputStream settingsFile) {
        Properties settings = new Properties();
        try {
            settings.load(settingsFile);
            init(settings);
            register(plugin);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public PiwikStatistics(final Properties settings) {
        init(settings);
    }
    private void init(final Properties settings) {
        try {
            String url = settings.getProperty("piwik_url");
            if (url.isEmpty()) {
                apiUrl = null;
                return;
            }
            apiUrl = new URL(url);
            idSite = Integer.parseInt(settings.getProperty("piwik_site_id"));
            String seconds = settings.getProperty("stats_interval");
            if (seconds != null && !seconds.isEmpty())
                wait = Long.parseLong(seconds) * TICKS_PER_SECOND;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid Piwik-URL defined", e);
        }
    }
    
    public static String getUniqueID() {
        return String.format("%016x", UUID.randomUUID().getMostSignificantBits());
    }
    
    public void register(final Plugin rplugin) {
        if (apiUrl == null)
            return;
        plugin = rplugin;
        //plugin.getServer().getPluginManager().registerEvents(new StatsListener(), plugin);
        pluginname = plugin.getName();
        version = plugin.getDescription().getVersion();
        
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                // Well, we all know it isn't http, but as piwik is a website tracking, it doesn't tracks the url if it isn't a http url ;)
                server = "http://" + StatsUtils.getIP(plugin.getServer()) + ":" + plugin.getServer().getPort();
                servername = ChatColor.stripColor(plugin.getServer().getServerName().replace(SEPERATOR, "-"));
                servermotd = ChatColor.stripColor(plugin.getServer().getMotd().replace(SEPERATOR, "-").replaceAll("\\s+", " "));
                trackEnable();
            }
        }, wait);
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                final int playercount = plugin.getServer().getOnlinePlayers().length;
                if (playercount > 0) {
                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                        @Override
                        public void run() {
                            trackOnlineUsage(playercount);
                        }
                    });
                }
            }
        }, wait, wait);
    }
    
    private void trackEnable() {
        Plugin[] pluginlist = plugin.getServer().getPluginManager().getPlugins();
        List<String[]> cdata = new ArrayList<String[]>();
        cdata.add(new String[]{"Server-Name", servername});
        cdata.add(new String[]{"Server-Version", (plugin.getServer().getName() + " " + plugin.getServer().getVersion())});
        cdata.add(new String[]{"Plugin-Version", pluginname + " " + version});
        
        Stack<StringBuilder> plugins = new Stack<StringBuilder>();
        plugins.add(new StringBuilder(""));
        
        for (Plugin cplugin : pluginlist) {
            StringBuilder currentPlugins = plugins.lastElement();
            if ((currentPlugins.length() + cplugin.getName().length() + 1) > MAX_CVAR_SIZE) {
                plugins.add(new StringBuilder());
                currentPlugins = plugins.lastElement();
            }
            if (currentPlugins.length() > 0)
                currentPlugins.append(",");
            currentPlugins.append(cplugin.getName());
        }
        for (int i = 0; i < plugins.size(); i++) {
            String plname = i == 0 ? "Plugins" : ("Plugins " + (i + 1));
            cdata.add(new String[]{plname, plugins.get(i).toString()});
        }
        cdata.add(new String[]{"Mode", plugin.getServer().getOnlineMode() ? "Online" : "Offline"});
        JSONObject cvar = getCVar(cdata.toArray(new String[cdata.size()][]));
        
        String[][] args = new String[][]{
            {"action_name", servermotd},
            {PIWIK_FIELD_CVAR, cvar.toJSONString()}
        };
        track(server + SEPERATOR + pluginname + "/load", args);
    }

    private void trackOnlineUsage(final int playercount) {
        List<String[]> cdata = new ArrayList<String[]>();
        cdata.add(new String[]{"Online-Count", Integer.toString(playercount)});
        if (!plugin.getServer().getOnlineMode())
            cdata.add(new String[]{"Offline-Mode", "yes"});
        JSONObject cvar = getCVar(cdata.toArray(new String[cdata.size()][]));
            
        track(server + SEPERATOR + pluginname + "/usage", new String[][]{
            {"multiple", Integer.toString(playercount)}, // handled by piwikProxy.php to create a Batch-Request to simulate multiple hits
            {PIWIK_FIELD_CVAR, cvar.toJSONString()}
        });
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getCVar(final String[][] cvars) {
        JSONObject cvar = new JSONObject();
        for (int i = 0; i < cvars.length; i++) {
            JSONArray t = new JSONArray();
            t.add(cvars[i][0]);
            t.add(cvars[i][1]);
            cvar.put(Integer.toString(i + 1), t);
        }
        return cvar;
    }
    
    protected void track(final String target, final String[][] addargs) {
        String[][] basicargs = new String[][]{
            {"idsite", Integer.toString(idSite)},
            {"rec", "1"},
            {"url", target},
            {"_id", serverid},
            {"rand", Long.toString(System.currentTimeMillis())},
            {"apiv", Integer.toString(APIV)},
        };
        
        String[][] arguments;
        if (addargs.length > 0) {
            arguments = new String[basicargs.length + addargs.length][2];
            System.arraycopy(basicargs, 0, arguments, 0, basicargs.length);
            System.arraycopy(addargs, 0, arguments, basicargs.length, addargs.length);
        } else {
            arguments = basicargs; 
        }

        try {
            URL req = StatsUtils.buildRequest(apiUrl, arguments);
            URLConnection conn = req.openConnection();
            //System.out.println(req.toString());
            conn.setUseCaches(false);
            conn.connect();
            InputStream in = conn.getInputStream();
            in.read();
            in.close();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /*class StatsListener implements Listener {
        @EventHandler
        public void onJoin(final PlayerJoinEvent event) {
            plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    track(server + SEPERATOR + pluginname + "/join", new String[0][0]);
                }
            });
        }
    }*/

    @Override
    public void trackEvent(final String event) {
        track(server + SEPERATOR + pluginname + SEPERATOR + event, new String[0][0]);
    }
}
