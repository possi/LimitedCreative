package de.jaschastarke.minecraft.limitedcreative.gmperm;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.RegisteredServiceProvider;

import de.jaschastarke.bukkit.lib.CoreModule;

public class PermissionInterface {
    //private CoreModule<?> mod;
    private Permission permission = null;
    private RegisteredServiceProvider<Permission> permissionProvider;
    
    public PermissionInterface(CoreModule<?> mod) {
        //this.mod = mod;
        permissionProvider = mod.getPlugin().getServer().getServicesManager().getRegistration(Permission.class);
    }
    
    public boolean isPresent() {
        return permissionProvider != null && permissionProvider.getProvider() != null;
    }
    
    public void clear() {
        permission = null;
    }
    
    public Permission getPermission() {
        if (permission == null)
            permission = permissionProvider.getProvider();
        return permission;
    }

}
