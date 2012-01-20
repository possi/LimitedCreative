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
package de.jaschastarke.minecraft.limitedcreative.regions;

import java.io.File;

import org.bukkit.GameMode;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;


import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

import de.jaschastarke.minecraft.limitedcreative.LCPlayer;
import de.jaschastarke.minecraft.limitedcreative.LimitedCreativeCore;
import de.jaschastarke.minecraft.worldguard.ApplicableRegions;
import de.jaschastarke.minecraft.worldguard.CRegionManager;
import de.jaschastarke.minecraft.worldguard.FlagList;
import static de.jaschastarke.minecraft.utils.Locale.L;

public class WorldGuardIntegration {
    public static LimitedCreativeCore plugin;
    public static WorldGuardPlugin wg;
    private CRegionManager rm;

    public WorldGuardIntegration(LimitedCreativeCore pplugin) {
        plugin = pplugin;
        wg = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
    }
    
    public static boolean available() {
        return LimitedCreativeCore.plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null;
    }
    

    public static final StateFlag CREATIVE = new StateFlag("creative", false);
    public static final RegionGroupFlag CREATIVE_GROUP = new RegionGroupFlag("creative-group", RegionGroupFlag.RegionGroup.MEMBERS);
    static {
        CREATIVE.setGroupFlag(CREATIVE_GROUP);
    }
    
    public void init() {
        rm = new CRegionManager(new File(plugin.getDataFolder(), "regions.yml"));
        
        FlagList.addFlag(CREATIVE);
        FlagList.addFlag(CREATIVE_GROUP);
        
        new WGIPlayerListen().register();
        new WGIBlockListen().register();
    }
    
    public CRegionManager getRegionManager() {
        return rm;
    }
    
    
    public class WGIBlockListen extends BlockListener {
        @Override
        public void onBlockBreak(BlockBreakEvent event) {
            if (event.isCancelled())
                return;
            LCPlayer player = LCPlayer.get(event.getPlayer());
            RegionManager mgr = wg.getGlobalRegionManager().get(event.getPlayer().getWorld());
            Vector pt = new Vector(event.getBlock().getLocation().getBlockX(), event.getBlock().getLocation().getBlockY(), event.getBlock().getLocation().getBlockZ());
            ApplicableRegions set = new ApplicableRegions(mgr.getApplicableRegions(pt), rm.world(event.getPlayer().getWorld()));
            
            boolean creative_region = set.allows(CREATIVE);
            plugin.logger.info("in creative region: "+Boolean.toString(creative_region)+" - "+event.getBlock().getLocation());
            if (creative_region && player.getRaw().getGameMode() != GameMode.CREATIVE) {
                plugin.spawnblock.block(event.getBlock(), player);
            }
        }

        @Override
        public void onBlockPlace(BlockPlaceEvent event) {
            if (event.isCancelled())
                return;
            LCPlayer player = LCPlayer.get(event.getPlayer());
            if (player.getRegionCreative()) {
                // do not build outside of creative regions, when in the region
                RegionManager mgr = wg.getGlobalRegionManager().get(event.getPlayer().getWorld());
                Vector pt = new Vector(event.getBlock().getLocation().getBlockX(), event.getBlock().getLocation().getBlockY(), event.getBlock().getLocation().getBlockZ());
                ApplicableRegions set = new ApplicableRegions(mgr.getApplicableRegions(pt), rm.world(event.getPlayer().getWorld()));
                if (!set.allows(CREATIVE, player)) { 
                    event.getPlayer().sendMessage(L("blocked.outside_creative"));
                    event.setCancelled(true);
                }
            }
        }

        @Override
        public void onBlockDispense(BlockDispenseEvent event) {
            plugin.logger.info("Block dispense: "+event.getBlock().getType()+" - "+event.getItem().getType());
        }

        private void register() {
            plugin.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, this, Priority.Normal, plugin);
            plugin.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACE, this, Priority.Normal, plugin);
            plugin.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_DISPENSE, this, Priority.Normal, plugin);
        }
    }
    public class WGIPlayerListen extends PlayerListener {
        @Override
        public void onPlayerMove(PlayerMoveEvent event) {
            if (event.isCancelled())
                return;
            if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                    || event.getFrom().getBlockY() != event.getTo().getBlockY()
                    || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {

                LCPlayer player = LCPlayer.get(event.getPlayer());
                RegionManager mgr = wg.getGlobalRegionManager().get(event.getPlayer().getWorld());
                Vector pt = new Vector(event.getTo().getBlockX(), event.getTo().getBlockY(), event.getTo().getBlockZ());
                ApplicableRegions set = new ApplicableRegions(mgr.getApplicableRegions(pt), rm.world(event.getPlayer().getWorld()));
                
                player.setRegionCreativeAllowed(set.allows(CREATIVE, player), event);
            }
        }
        
        private void register() {
            plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, this, Priority.Normal, plugin);
        }
    }
}
