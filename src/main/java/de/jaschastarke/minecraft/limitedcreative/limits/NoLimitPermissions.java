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
package de.jaschastarke.minecraft.limitedcreative.limits;

import java.util.Collection;

import org.bukkit.block.Block;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.material.MaterialData;
import org.bukkit.permissions.PermissionDefault;

import de.jaschastarke.maven.ArchiveDocComments;
import de.jaschastarke.maven.PluginPermissions;
import de.jaschastarke.minecraft.lib.permissions.BasicPermission;
import de.jaschastarke.minecraft.lib.permissions.DynamicPermission;
import de.jaschastarke.minecraft.lib.permissions.IAbstractPermission;
import de.jaschastarke.minecraft.lib.permissions.IDynamicPermission;
import de.jaschastarke.minecraft.lib.permissions.IPermission;
import de.jaschastarke.minecraft.lib.permissions.IPermissionContainer;
import de.jaschastarke.minecraft.lib.permissions.IsChildPermission;
import de.jaschastarke.minecraft.lib.permissions.ParentPermissionContainerNode;
import de.jaschastarke.minecraft.lib.permissions.SimplePermissionContainerNode;
import de.jaschastarke.minecraft.limitedcreative.Permissions;

@ArchiveDocComments
public class NoLimitPermissions extends SimplePermissionContainerNode {
    public NoLimitPermissions(IAbstractPermission parent, String name) {
        super(parent, name);
    }
    
    @PluginPermissions
    public static final IPermissionContainer PARENT = new NoLimitPermissions(Permissions.CONTAINER, "nolimit");
    
    /**
     * Grants bypassing of all nolimit-permissions.
     */
    public static final IPermission ALL = new ParentPermissionContainerNode(PARENT, "*", PermissionDefault.OP, PARENT);

    /**
     * Allows bypassing the "do not open a chest" and "do not open inventory"-limitation
     */
    @IsChildPermission
    public static final IPermission CHEST = new BasicPermission(PARENT, "chest", PermissionDefault.FALSE);
    /**
     * Allows bypassing the "do not drop anything"-limitation
     */
    @IsChildPermission
    public static final IPermission DROP = new BasicPermission(PARENT, "drop", PermissionDefault.FALSE);
    /**
     * Allows bypassing the "do not pickup anything"-limitation
     */
    @IsChildPermission
    public static final IPermission PICKUP = new BasicPermission(PARENT, "pickup", PermissionDefault.FALSE);
    /**
     * Allows bypassing the "no pvp"-limitation
     */
    @IsChildPermission
    public static final IPermission PVP = new BasicPermission(PARENT, "pvp", PermissionDefault.FALSE);
    /**
     * Allows bypassing the "no dealing damage to creatures"-limitation
     */
    @IsChildPermission
    public static final IPermission MOB_DAMAGE = new BasicPermission(PARENT, "mob_damage", PermissionDefault.FALSE);
    /**
     * Allows bypassing the "do not interact with specific blocks"-limitation
     */
    @IsChildPermission
    public static final IPermission BASE_INTERACT = new BasicPermission(PARENT, "interact", PermissionDefault.FALSE);
    /**
     * Allows bypassing the "block place/item use"-limitation
     */
    @IsChildPermission
    public static final IPermission BASE_USE = new BasicPermission(PARENT, "use", PermissionDefault.FALSE);
    /**
     * Allows bypassing the "block break"-limitation
     */
    @IsChildPermission
    public static final IPermission BASE_BREAK = new BasicPermission(PARENT, "break", PermissionDefault.FALSE);
    
    /**
     * Allows bypassing the "don't change heal/food-state"-limitation
     */
    @IsChildPermission
    public static final IPermission STATS_HEALTH = new BasicPermission(PARENT, "health", PermissionDefault.FALSE);
    
    /**
     * Allows bypassing the "don't get xp/level"-limitation
     */
    @IsChildPermission
    public static final IPermission STATS_XP = new BasicPermission(PARENT, "xp", PermissionDefault.FALSE);
    
    /**
     * Allows bypassing the "remove all effects on leaving creative"-limitation
     */
    @IsChildPermission
    public static final IPermission STATS_POTION = new BasicPermission(PARENT, "potion", PermissionDefault.FALSE);

    public static IDynamicPermission INVENTORY(Inventory inv) {
        return new InventoryPermission(CHEST, inv.getType());
    }
    public static IDynamicPermission INVENTORY(InventoryType invtype) {
        return new InventoryPermission(CHEST, invtype);
    }
    public static IDynamicPermission INTERACT(Block block) {
        return new MaterialPermission(BASE_INTERACT, block.getState().getData());
    }
    public static IDynamicPermission USE(Block block) {
        return new MaterialPermission(BASE_USE, block.getState().getData());
    }
    public static IDynamicPermission USE(MaterialData m) {
        return new MaterialPermission(BASE_USE, m);
    }
    public static IDynamicPermission BREAK(Block block) {
        return new MaterialPermission(BASE_BREAK, block.getState().getData());
    }

    
    public static class InventoryPermission extends DynamicPermission {
        private InventoryType it;
        public InventoryPermission(IAbstractPermission parent, InventoryType t) {
            super(parent);
            it = t;
        }

        @Override
        protected void buildPermissionsToCheck(Collection<IAbstractPermission> perms) {
            perms.add(new BasicPermission(parent, it.toString()));
        }
    }
    public static class MaterialPermission extends DynamicPermission {
        private MaterialData md;
        public MaterialPermission(IAbstractPermission parent, MaterialData m) {
            super(parent);
            md = m;
        }

        @Override
        protected void buildPermissionsToCheck(Collection<IAbstractPermission> perms) {
            perms.add(new BasicPermission(parent, md.getItemType().toString()));
            perms.add(new BasicPermission(parent, md.getItemType().toString() + IAbstractPermission.SEP + md.getData()));
        }
    }
}
