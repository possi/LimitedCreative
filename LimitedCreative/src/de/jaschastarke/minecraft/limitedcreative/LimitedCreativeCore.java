package de.jaschastarke.minecraft.limitedcreative;
import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;


public class LimitedCreativeCore extends JavaPlugin {
    public final Logger logger = Logger.getLogger("Minecraft");
    public Configuration config;
    public WorldGuardIntegration worldguard;
    public static LimitedCreativeCore plugin;

    @Override
    public void onDisable() {
        logger.info("["+this.getDescription().getName()+"] cleanly unloaded.");
    }

    @Override
    public void onEnable() {
        plugin = this;
        
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
}
