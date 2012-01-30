package de.jaschastarke.minecraft.worldedit;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.wepif.PermissionsResolverManager;

public class PermissionsBridge {
    protected com.sk89q.wepif.PermissionsResolverManager pif;
    public PermissionsBridge(JavaPlugin plugin) {
        PermissionsResolverManager.initialize(plugin);
        pif = PermissionsResolverManager.getInstance();
    }
    public boolean hasPermission(OfflinePlayer sender, String permission) {
        return pif.hasPermission(sender, permission);
    }
}
