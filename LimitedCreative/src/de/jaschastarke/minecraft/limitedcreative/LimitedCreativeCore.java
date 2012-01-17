package de.jaschastarke.minecraft.limitedcreative;
import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;


public class LimitedCreativeCore extends JavaPlugin {
    public final Logger logger = Logger.getLogger("Minecraft");
    public Configuration config;
    public WorldGuardIntegration worldguard;
    public static LimitedCreativeCore plugin;
    
    public static boolean serializeFallBack = false;

    @Override
    public void onDisable() {
        logger.info("["+this.getDescription().getName()+"] cleanly unloaded.");
    }

    @Override
    public void onEnable() {
        plugin = this;
        
        serializeFallBack = versionCompare(getServer().getBukkitVersion().replaceAll("-.*$", ""), "1.1") < 0;
        
        config = new Configuration(this.getConfig());
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
}
