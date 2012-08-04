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

import de.jaschastarke.minecraft.utils.IPermission;

public enum Perms implements IPermission {
    CONFIG("config"),
    REGIONS("regions"),
    REGIONS_BYPASS("regions_bypass"),
    GM("switch_gamemode"),
    GM_BACKONLY("switch_gamemode.backonly"),
    GM_SURVIVAL("switch_gamemode.survival"),
    GM_CREATIVE("switch_gamemode.creative"),
    GM_ADVENTURE("switch_gamemode.adventure"),
    GM_OTHER("switch_gamemode.other"),
    KEEPINVENTORY("keepinventory");
    
    private static final String NS = "limitedcreative";
    
    private String perm;
    private Perms(String permission) {
        perm = permission;
    }
    @Override
    public String toString() {
        return NS + SEP + perm;
    }
    
    public enum NoLimit implements IPermission {
        DROP("drop"),
        PICKUP("pickup"),
        CHEST("chest"),
        SIGN("sign"),
        BUTTON("button"),
        LEVER("lever"),
        PVP("pvp"),
        MOB_DAMAGE("mob_damage"),
        USE("use"),
        BREAK("break");
        
        private static final String NS = "nolimit";
        
        private String perm;
        private NoLimit(String permission) {
            perm = permission;
        }
        @Override
        public String toString() {
            return Perms.NS + SEP + NoLimit.NS + SEP + perm;
        }
    }
}
