package de.jaschastarke.minecraft.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import de.jaschastarke.minecraft.limitedcreative.LimitedCreativeCore;
import de.jaschastarke.minecraft.worldedit.PermissionsBridge;

public class Permissions {
    protected JavaPlugin plugin = null;
    protected PermissionsBridge pif = null;
    
    public Permissions(JavaPlugin plugin) {
        this.plugin = plugin;
        try {
            // because worldedit may be not loaded as plugin, just as library, we check that way
            Class.forName("com.sk89q.wepif.PermissionsResolverManager", false, plugin.getClass().getClassLoader());
            pif = new PermissionsBridge(plugin);
        } catch (ClassNotFoundException e) {}
    }
    
    public boolean hasPermission(Player player, String permission) {
        boolean ret = false;
        if (pif != null) {
            ret = pif.hasPermission(player, permission);
        } else {
            // bukkit permission fallback
            ret = player.hasPermission(permission);
        }
        debug(player, permission, ret);
        return ret;
    }
    
    public boolean hasPermission(CommandSender sender, String permission) {
        if (sender instanceof Player) {
            return hasPermission((Player) sender, permission);
        } else {
            debug(sender, permission, true);
            return true;
        }
    }
    
    private void debug(CommandSender player, String permission, boolean result) {
        if (plugin instanceof LimitedCreativeCore && ((LimitedCreativeCore) plugin).config.getDebug())
            LimitedCreativeCore.debug("hasPermission: " + player.getName() + " - " + permission + " - " + result);
    }
}
