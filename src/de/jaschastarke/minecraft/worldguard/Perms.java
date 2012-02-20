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
package de.jaschastarke.minecraft.worldguard;

import de.jaschastarke.minecraft.utils.IPermission;

@Deprecated // NOT USED YET
public enum Perms implements IPermission {
    INFO("info"),
    INFO_OWN("info.own"),
    INFO_MEMBER("info.member");
    
    private static final String NS = "worldguard.region";
    
    private String perm;
    private Perms(String permission) {
        perm = permission;
    }
    @Override
    public String toString() {
        return NS + SEP + perm;
    }
}
