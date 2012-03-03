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
package de.jaschastarke.minecraft.limitedcreative.listeners;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import de.jaschastarke.minecraft.limitedcreative.Core;

public class CreativeBlockListener implements Listener {
    private Core plugin;
    public CreativeBlockListener(Core plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(ignoreCancelled=true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            if (event.getBlock().hasMetadata("created_gamemode")) {
                List<MetadataValue> metadata = event.getBlock().getMetadata("created_gamemode");
                for (MetadataValue m : metadata) {
                    if (m.getOwningPlugin().equals(plugin)) {
                        if (GameMode.valueOf(m.asString()) == GameMode.CREATIVE) {
                            plugin.spawnblock.block(event.getBlock());
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onBlockPlace(BlockPlaceEvent event) {
        MetadataValue test = new FixedMetadataValue(plugin, event.getPlayer().getGameMode().name());
        Core.debug("test: " + test.value().toString() + " - " + test.asString());
        
        event.getBlock().setMetadata("created_by", new FixedMetadataValue(plugin, event.getPlayer().getName()));
        event.getBlock().setMetadata("created_gamemode", new FixedMetadataValue(plugin, event.getPlayer().getGameMode().name()));
        event.getBlock().setMetadata("created_at", new FixedMetadataValue(plugin, System.currentTimeMillis()));
        
        Core.debug(event.getBlock() + ": metadata created_gamemode: " + event.getBlock().getMetadata("created_gamemode").get(0).value().toString());
    }
}
