/*
 * Limited Creative - (Bukkit Plugin)
 * Copyright (C) 2012 jascha@ja-s.de
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.jaschastarke.minecraft.limitedcreative.blockstate;

import org.bukkit.permissions.PermissionDefault;

import de.jaschastarke.maven.ArchiveDocComments;
import de.jaschastarke.maven.PluginPermissions;
import de.jaschastarke.minecraft.lib.permissions.BasicPermission;
import de.jaschastarke.minecraft.lib.permissions.IAbstractPermission;
import de.jaschastarke.minecraft.lib.permissions.IPermission;
import de.jaschastarke.minecraft.lib.permissions.IPermissionContainer;
import de.jaschastarke.minecraft.lib.permissions.SimplePermissionContainerNode;
import de.jaschastarke.minecraft.limitedcreative.Permissions;

@ArchiveDocComments
public class BlockStatePermissions extends SimplePermissionContainerNode {
    public BlockStatePermissions(IAbstractPermission parent, String name) {
        super(parent, name);
    }
    
    @PluginPermissions
    public static final IPermissionContainer PARENT = new BlockStatePermissions(Permissions.CONTAINER, "blockstate");
    
    /**
     * Grants ability to use the configured tool to get info about placed blocks.
     */
    public static final IPermission TOOL = new BasicPermission(PARENT, "tool", PermissionDefault.OP);
    
    /**
     * Grants access to the blockstate admin command to modify the database.
     */
    public static final IPermission COMMAND = new BasicPermission(PARENT, "command", PermissionDefault.OP);
    
    /**
     * Allows to get drops even if a block was created from a creative player or WorldEdit.
     */
    public static final IPermission BYPASS = new BasicPermission(PARENT, "bypass", PermissionDefault.FALSE);
}
