package de.jaschastarke.minecraft.limitedcreative.limits;

import org.bukkit.permissions.PermissionDefault;

import de.jaschastarke.minecraft.lib.permissions.IAbstractPermission;
import de.jaschastarke.minecraft.lib.permissions.IPermissionContainer;
import de.jaschastarke.minecraft.lib.permissions.IPermission;
import de.jaschastarke.minecraft.limitedcreative.Perms;

public enum NoLimitPerms implements IPermission {
    DROP("drop", PermissionDefault.FALSE),
    PICKUP("pickup", PermissionDefault.FALSE),
    CHEST("chest", PermissionDefault.FALSE),
    SIGN("sign", PermissionDefault.FALSE),
    BUTTON("button", PermissionDefault.FALSE),
    LEVER("lever", PermissionDefault.FALSE),
    PVP("pvp", PermissionDefault.FALSE),
    MOB_DAMAGE("mob_damage", PermissionDefault.FALSE),
    USE("use", PermissionDefault.FALSE),
    BREAK("break", PermissionDefault.FALSE);
    
    public static final IPermissionContainer Parent = new IPermissionContainer() {
        @Override
        public IAbstractPermission getParent() {
            return Perms.Root;
        }
        
        @Override
        public IPermission[] getPermissions() {
            return NoLimitPerms.values();
        }
        
        @Override
        public String getFullString() {
            return "nolimit";
        }
    };
    
    private String perm;
    private PermissionDefault def;
    private NoLimitPerms(String permission, PermissionDefault pdefault) {
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
