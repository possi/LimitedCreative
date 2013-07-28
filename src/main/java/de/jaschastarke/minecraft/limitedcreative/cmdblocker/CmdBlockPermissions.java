package de.jaschastarke.minecraft.limitedcreative.cmdblocker;

import java.util.Collection;

import org.bukkit.permissions.PermissionDefault;

import de.jaschastarke.maven.ArchiveDocComments;
import de.jaschastarke.minecraft.lib.permissions.BasicPermission;
import de.jaschastarke.minecraft.lib.permissions.DynamicPermission;
import de.jaschastarke.minecraft.lib.permissions.IAbstractPermission;
import de.jaschastarke.minecraft.lib.permissions.IDynamicPermission;
import de.jaschastarke.minecraft.lib.permissions.IPermission;
import de.jaschastarke.minecraft.lib.permissions.SimplePermissionContainerNode;
import de.jaschastarke.minecraft.limitedcreative.Permissions;

@ArchiveDocComments
public class CmdBlockPermissions extends SimplePermissionContainerNode {
    public CmdBlockPermissions(IAbstractPermission parent, String name) {
        super(parent, name);
    }
    
    public static final SimplePermissionContainerNode CONTAINER = new CmdBlockPermissions(Permissions.CONTAINER, "cmdblock");
    
    /**
     * Allows bypassing the "command block"-limitation. So no commands are blocked for this users.
     */
    public static final IPermission ALL = new BasicPermission(CONTAINER, "*", PermissionDefault.OP);
    

    /**
     * Allows to bypass specific blockings of commands as it tests against all partial permissions:
     * 
     * Example:
     * A Command "/execute a fuzzy command -n 256" is entered by the player which is blocked by the configuration the 
     * following permissions are tested, and if one is present for the user, he is allowed to execute the command:
     *  - limitedcreative.cmdblock.*
     *  - limitedcreative.cmdblock.execute
     *  - limitedcreative.cmdblock.execute.a
     *  - limitedcreative.cmdblock.execute.a.fuzzy
     *  - limitedcreative.cmdblock.execute.a.fuzzy.command
     *  - limitedcreative.cmdblock.execute.a.fuzzy.command.-n
     *  - limitedcreative.cmdblock.execute.a.fuzzy.command.-n.256
     */
    public static IDynamicPermission COMMAND(String cmd) {
        return new CommandPermission(ALL, cmd);
    }
    
    public static class CommandPermission extends DynamicPermission {
        private String cmd;
        public CommandPermission(IAbstractPermission parent, String cmd) {
            super(parent);
            this.cmd = cmd;
        }

        @Override
        protected void buildPermissionsToCheck(Collection<IAbstractPermission> perms) {
            String[] chunks = cmd.split("\\s+");
            String chain = "";
            for (String chunk : chunks) {
                if (chain.length() > 0)
                    chain += IAbstractPermission.SEP;
                chain += chunk;
                perms.add(new BasicPermission(CONTAINER, chain));
            }
        }
    }
}
