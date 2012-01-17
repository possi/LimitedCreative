package de.jaschastarke.minecraft.limitedcreative;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
//import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;

public class WorldGuardIntegration {
    public static LimitedCreativeCore plugin;
    public static WorldGuardPlugin wg;

    public WorldGuardIntegration(LimitedCreativeCore pplugin) {
        plugin = pplugin;
        wg = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
    }
    

    public static final StateFlag CREATIVE_MEMBER = new StateFlag("creative-member", false);
    
    public void init() {
        
    }
}
