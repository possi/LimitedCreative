package de.jaschastarke.minecraft.limitedcreative;

import org.bukkit.permissions.PermissionDefault;

import de.jaschastarke.maven.ArchiveDocComments;
import de.jaschastarke.maven.PluginPermissions;
import de.jaschastarke.minecraft.lib.permissions.BasicPermission;
import de.jaschastarke.minecraft.lib.permissions.IAbstractPermission;
import de.jaschastarke.minecraft.lib.permissions.IsChildPermission;
import de.jaschastarke.minecraft.lib.permissions.ParentPermissionContainerNode;

@ArchiveDocComments
public class SwitchGameModePermissions extends ParentPermissionContainerNode {
    /**
     * Allows switching of own game mode to creative/adventure and back
     */
    @PluginPermissions
    public final static SwitchGameModePermissions ALL = new SwitchGameModePermissions(Permissions.CONTAINER, "switch_gamemode");
    
    protected SwitchGameModePermissions(IAbstractPermission parent, String name) {
        super(parent, name);
    }
    
    @Override
    public PermissionDefault getDefault() {
        return PermissionDefault.OP;
    }
    
    /**
     * Allows switching of own game mode to default of the not world he is in, but not to an other
     */
    public final static BasicPermission BACKONLY = new BasicPermission(ALL, "backonly", PermissionDefault.FALSE);
    
    /**
     * Allows switching of own game mode to survival, but not to creative/adventure
     */
    @IsChildPermission
    public final static BasicPermission SURVIVAL = new BasicPermission(ALL, "survival", PermissionDefault.FALSE);
    /**
     * Allows switching of own game mode to creative, but not to survival/adventure
     */
    @IsChildPermission
    public final static BasicPermission CREATIVE = new BasicPermission(ALL, "creative", PermissionDefault.FALSE);
    /**
     * Allows switching of own game mode to adventure, but not to creative/survival
     */
    @IsChildPermission
    public final static BasicPermission ADVENTURE = new BasicPermission(ALL, "adventure", PermissionDefault.FALSE);
    /**
     * Allows switching of own game mode to spectator, but not to creative/survival/adventure
     */
    @IsChildPermission
    public final static BasicPermission SPECTATOR = new BasicPermission(ALL, "spectator", PermissionDefault.FALSE);
    
    /**
     * Allows switching of other users game mode
     */
    public final static BasicPermission OTHER = new BasicPermission(ALL, "other", PermissionDefault.OP);
}
