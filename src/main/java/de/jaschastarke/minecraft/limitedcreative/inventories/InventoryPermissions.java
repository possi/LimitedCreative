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
package de.jaschastarke.minecraft.limitedcreative.inventories;

import org.bukkit.permissions.PermissionDefault;

import de.jaschastarke.maven.ArchiveDocComments;
import de.jaschastarke.maven.PluginPermissions;
import de.jaschastarke.minecraft.lib.permissions.BasicPermission;
import de.jaschastarke.minecraft.lib.permissions.IPermission;
import de.jaschastarke.minecraft.lib.permissions.SimplePermissionContainer;
import de.jaschastarke.minecraft.limitedcreative.Permissions;

/**
 * 
 * @internal Doesn't represent a node, is only a list of additional permissions with an other parent.
 *
 */
@ArchiveDocComments
@PluginPermissions
public class InventoryPermissions extends SimplePermissionContainer {
    
    /**
     * Allows bypassing the inventory separation
     */
    public static final IPermission KEEP_INVENTORY = new BasicPermission(Permissions.CONTAINER, "keepinventory", PermissionDefault.FALSE);
    
    /**
     * Allows bypassing creative armor settings. No armor is changed on going to creative.
     */
    public static final IPermission BYPASS_CREATIVE_ARMOR = new BasicPermission(Permissions.CONTAINER, "bypass_creativearmor", PermissionDefault.FALSE);
}
