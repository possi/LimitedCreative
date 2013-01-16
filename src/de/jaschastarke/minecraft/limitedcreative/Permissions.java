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
package de.jaschastarke.minecraft.limitedcreative;

import org.bukkit.permissions.PermissionDefault;

import de.jaschastarke.Singleton;
import de.jaschastarke.maven.ArchiveDocComments;
import de.jaschastarke.minecraft.lib.permissions.BasicPermission;
import de.jaschastarke.minecraft.lib.permissions.IPermission;
import de.jaschastarke.minecraft.lib.permissions.SimplePermissionContainer;

@ArchiveDocComments
public class Permissions extends SimplePermissionContainer implements Singleton {
    private static Permissions instance = new Permissions();
    public static Permissions getInstance() {
        return instance;
    }
    
    /**
     * Allows changing plugin configuration ingame via commands
     */
    public static final IPermission CONFIG = new BasicPermission(instance, "config", PermissionDefault.OP);
    
    @Override
    public String getFullString() {
        return "limitedcreative";
    }
}
