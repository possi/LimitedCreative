package de.jaschastarke.minecraft.limitedcreative.cmdblock;

import org.bukkit.permissions.PermissionDefault;

import de.jaschastarke.minecraft.lib.permissions.IAbstractPermission;
import de.jaschastarke.minecraft.lib.permissions.IPermissionContainer;
import de.jaschastarke.minecraft.lib.permissions.IPermission;
import de.jaschastarke.minecraft.limitedcreative.Perms;

public enum CmdBlockPerms implements IPermission {
    ALL("all", PermissionDefault.FALSE),;
    
    public static final IPermissionContainer Parent = new IPermissionContainer() {
        @Override
        public IAbstractPermission getParent() {
            return Perms.Root;
        }
        
        @Override
        public IPermission[] getPermissions() {
            return CmdBlockPerms.values();
        }
        
        @Override
        public String getFullString() {
            return "cmdblock";
        }
    };
    
    private String perm;
    private PermissionDefault def;
    private CmdBlockPerms(String permission, PermissionDefault pdefault) {
        perm = permission;
        def = pdefault;
    }
    @Override
    public IAbstractPermission getParent() {
        return Parent;
    }
    @Override
    public PermissionDefault getDefault() {
        return def;
    }
    @Override
    public String getFullString() {
        return getParent().getFullString() + SEP + perm;
    }
}
