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

import org.bukkit.GameMode;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;

import de.jaschastarke.minecraft.lib.permissions.IAbstractPermission;
import de.jaschastarke.minecraft.limitedcreative.ModCreativeLimits;

public class EntityListener implements Listener {
    private ModCreativeLimits mod;
    public EntityListener(ModCreativeLimits mod) {
        this.mod = mod;
    }
    
    /**
     * don't let the player be target by creatures he can't kill
     */
    @EventHandler
    public void onEntityTargetPlayer(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player && !event.isCancelled()) {
            if (event.getEntity() instanceof Creature) {
                if (((Player) event.getTarget()).getGameMode() == GameMode.CREATIVE && mod.getConfig().getBlockDamageMob()) {
                    if (!checkPermission((Player) event.getTarget(), NoLimitPermissions.MOB_DAMAGE)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() != null && event.getDroppedExp() > 0) {
            if (mod.getNoDropMobs().isXPPrevented(event.getEntity())) {
                event.setDroppedExp(0);
                event.getDrops().clear();
            }
        }
        if (event.getDrops().size() > 0 && mod.getNoDropMobs().isDropPrevented(event.getEntity())) {
            event.getDrops().clear();
        }
        mod.getNoDropMobs().remove(event.getEntity());
    }
    
    private boolean checkPermission(Player player, IAbstractPermission perm) {
        return mod.getPlugin().getPermManager().hasPermission(player, perm);
    }
}
