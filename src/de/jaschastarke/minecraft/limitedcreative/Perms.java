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

import de.jaschastarke.maven.ArchiveDocComments;
import de.jaschastarke.minecraft.lib.annotations.PermissionDescripted;
import de.jaschastarke.minecraft.lib.permissions.IAbstractPermission;
import de.jaschastarke.minecraft.lib.permissions.IPermissionContainer;
import de.jaschastarke.minecraft.lib.permissions.IPermission;

@PermissionDescripted(PermissionDescripted.Type.STATIC_ATTRIBUTES)
@ArchiveDocComments
public enum Perms implements IPermission {
    
    /**
     * Test123
     */
    CONFIG("config", PermissionDefault.OP),
    
    /**
     * Test456
     */
    REGIONS("regions", PermissionDefault.OP),
    REGIONS_BYPASS("regions_bypass", PermissionDefault.FALSE),
    GM("switch_gamemode", PermissionDefault.OP),
    GM_BACKONLY("switch_gamemode.backonly", PermissionDefault.FALSE),
    GM_SURVIVAL("switch_gamemode.survival", PermissionDefault.FALSE),
    GM_CREATIVE("switch_gamemode.creative", PermissionDefault.FALSE),
    GM_ADVENTURE("switch_gamemode.adventure", PermissionDefault.FALSE),
    GM_OTHER("switch_gamemode.other", PermissionDefault.OP),
    KEEPINVENTORY("keepinventory", PermissionDefault.FALSE);
    
    public static final IPermissionContainer Root = new IPermissionContainer() {
        @Override
        public IPermission[] getPermissions() {
            return Perms.values();
        }
        @Override
        public String getFullString() {
            return "limitedcreative";
        }
        @Override
        public IAbstractPermission getParent() {
            return null;
        }
    };
    
    private String perm;
    private PermissionDefault def;
    private Perms(String permission, PermissionDefault pdefault) {
        perm = permission;
        def = pdefault;
    }
    
    @Override
    public IAbstractPermission getParent() {
        return Root;
    }
    @Override
    public String getFullString() {
        return getParent().getFullString() + SEP + perm;
    }
    @Override
    public PermissionDefault getDefault() {
        return def;
    }
}
