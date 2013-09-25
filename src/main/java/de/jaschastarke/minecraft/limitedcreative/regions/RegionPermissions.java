package de.jaschastarke.minecraft.limitedcreative.regions;

import org.bukkit.permissions.PermissionDefault;

import de.jaschastarke.maven.ArchiveDocComments;
import de.jaschastarke.maven.PluginPermissions;
import de.jaschastarke.minecraft.lib.permissions.BasicPermission;
import de.jaschastarke.minecraft.lib.permissions.IAbstractPermission;
import de.jaschastarke.minecraft.lib.permissions.IPermission;
import de.jaschastarke.minecraft.lib.permissions.SimplePermissionContainerNode;
import de.jaschastarke.minecraft.limitedcreative.Permissions;

/**
 * Allows usage of the //region commands
 */
@ArchiveDocComments
final public class RegionPermissions extends SimplePermissionContainerNode implements IPermission {
    private RegionPermissions(IAbstractPermission parent, String name) {
        super(parent, name);
    }
    
    @Override
    public PermissionDefault getDefault() {
        return PermissionDefault.OP;
    }
    
    /**
     * Grants access to the /lcr command, which allows to define Limited Creatives region-flags 
     */
    @PluginPermissions
    public static final RegionPermissions REGION = new RegionPermissions(Permissions.CONTAINER, "region");

    /**
     * Ignores the force of a gamemode, when region optional is disabled
     */
    public static final IPermission BYPASS = new BasicPermission(REGION, "bypass", PermissionDefault.FALSE);
}
