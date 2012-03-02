package de.jaschastarke.minecraft.limitedcreative.regions;

import static de.jaschastarke.minecraft.utils.Locale.L;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

import de.jaschastarke.minecraft.limitedcreative.Core;
import de.jaschastarke.minecraft.limitedcreative.LCPlayer;
import de.jaschastarke.minecraft.limitedcreative.Players;
import de.jaschastarke.minecraft.utils.Util;
import de.jaschastarke.minecraft.worldguard.ApplicableRegions;
import de.jaschastarke.minecraft.worldguard.CRegionManager;
import de.jaschastarke.minecraft.worldguard.events.PlayerNewLocationAreaEvent;
import de.jaschastarke.minecraft.worldguard.events.PlayerSetAreaEvent;
import de.jaschastarke.minecraft.worldguard.events.PlayerUpdateAreaEvent;

public class RegionListener implements Listener {
    private static Core plugin = WorldGuardIntegration.plugin;
    private CRegionManager rm;
    public RegionListener(WorldGuardIntegration wgi) {
        rm = wgi.getRegionManager();
    }

    private ApplicableRegions regionSet(Location loc) {
        return rm.getRegionSet(loc);
    }
    private ApplicableRegions regionSet(Block block) {
        return rm.getRegionSet(block);
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        
        LCPlayer player = Players.get(event.getPlayer());
        boolean diffrent_region = rm.isDiffrentRegion(event.getPlayer(), event.getBlock().getLocation());
        
        if (player.isRegionGameMode() && diffrent_region) {
            // do not break outside of "gamemod-change-region" when in the region
            if (rm.getRegionSet(event.getBlock()).getFlag(Flags.GAMEMODE, event.getPlayer()) != player.getRegionGameMode()) {
                event.getPlayer().sendMessage(L("blocked.outside_break"));
                event.setCancelled(true);
            }
        } else if (diffrent_region) {
            // do not break inside of "survial-region in creative world" when outside
            if (rm.getRegionSet(event.getBlock()).getFlag(Flags.GAMEMODE) != null) {
                event.getPlayer().sendMessage(L("blocked.inside_break"));
                event.setCancelled(true);
            }
        }
        if (!event.isCancelled()) {
            // prevent any drops for survival players in creative regions
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE && rm.getRegionSet(event.getBlock()).getFlag(Flags.GAMEMODE) == GameMode.CREATIVE) {
                plugin.spawnblock.block(event.getBlock(), player);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;
        
        LCPlayer player = Players.get(event.getPlayer());
        boolean diffrent_region = rm.isDiffrentRegion(event.getPlayer(), event.getBlock().getLocation());
        
        if (player.isRegionGameMode() && diffrent_region) {
            // do not build outside of "gamemod-change-region" when in the region
            if (rm.getRegionSet(event.getBlock()).getFlag(Flags.GAMEMODE, event.getPlayer()) != player.getRegionGameMode()) { 
                event.getPlayer().sendMessage(L("blocked.outside_place"));
                event.setCancelled(true);
            }
        } else if (diffrent_region) {
            // do not build inside of "survial-region in creative world" when outside
            if (rm.getRegionSet(event.getBlock()).getFlag(Flags.GAMEMODE) != null) {
                event.getPlayer().sendMessage(L("blocked.inside_place"));
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerChangedArea(PlayerNewLocationAreaEvent event) {
        Players.get(event.getPlayer()).setRegionCreativeAllowed(event.getRegionSet().getFlag(Flags.GAMEMODE, event.getPlayer()) != null, event);
    }
    
    @EventHandler
    public void onPlayerSetArea(PlayerSetAreaEvent event) {
        Players.get(event.getPlayer()).setRegionCreativeAllowed(event.getRegionSet().getFlag(Flags.GAMEMODE, event.getPlayer()) != null, event);
    }
    
    @EventHandler
    public void onPlayerUpdateArea(PlayerUpdateAreaEvent event) {
        Players.get(event.getPlayer()).setRegionCreativeAllowed(event.getRegionSet().getFlag(Flags.GAMEMODE, event.getPlayer()) != null, event);
    }
    
    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event.isCancelled())
            return;
        
        Block source = event.getBlock().getRelative(event.getDirection());
        Core.debug("PistonExtend "+source.getType()+" "+event.getDirection());
        if (source.getType() != Material.AIR) {
            if (regionSet(source).getFlag(Flags.GAMEMODE) == GameMode.CREATIVE) {
                for (int i = 1; i <= 12; i++) {
                    Block dest = source.getRelative(event.getDirection(), i);
                    Core.debug("dest "+i+": "+dest.getType());
                    if (regionSet(dest).getFlag(Flags.GAMEMODE) != GameMode.CREATIVE) {
                        plugin.logger.warning(L("blocked.piston", source.getRelative(event.getDirection(), i - 1).getType().toString(), Util.toString(source.getLocation())));
                        event.setCancelled(true);
                        break;
                    } else if (dest.getType() == Material.AIR) {
                        break;
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event.isCancelled())
            return;
        Block source = event.getBlock().getRelative(event.getDirection(), 2);
        Block dest = source.getRelative(event.getDirection().getOppositeFace());
        Core.debug("PistonRetract "+source.getType()+" "+event.getDirection() + " " + event.isSticky());
        if (event.isSticky() && source.getType() != Material.AIR) { 
            Core.debug("dest "+dest.getType());
            if (regionSet(source).getFlag(Flags.GAMEMODE) == GameMode.CREATIVE) {
                if (regionSet(dest).getFlag(Flags.GAMEMODE) != GameMode.CREATIVE) {
                    plugin.logger.warning(L("blocked.piston", source.getType().toString(), Util.toString(source.getLocation())));
                    event.setCancelled(true);
                }
            } else if (regionSet(dest).getFlag(Flags.GAMEMODE) == GameMode.CREATIVE) {
                // source isn't creative
                plugin.logger.warning(L("blocked.piston_in", source.getType().toString(), Util.toString(source.getLocation())));
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (event.isCancelled())
            return;
        if (event.getEntity() instanceof Item) {
            if (!regionSet(event.getLocation()).allows(Flags.SPAWNDROPS))
                event.setCancelled(true);
        }
    }
}
