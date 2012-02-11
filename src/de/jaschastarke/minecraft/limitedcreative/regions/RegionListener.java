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

import de.jaschastarke.minecraft.limitedcreative.LCPlayer;
import de.jaschastarke.minecraft.limitedcreative.LimitedCreativeCore;
import de.jaschastarke.minecraft.utils.Util;
import de.jaschastarke.minecraft.worldguard.ApplicableRegions;
import de.jaschastarke.minecraft.worldguard.CRegionManager;
import de.jaschastarke.minecraft.worldguard.events.PlayerChangedAreaEvent;

public class RegionListener implements Listener {
    private static LimitedCreativeCore plugin = WorldGuardIntegration.plugin;
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
        LCPlayer player = LCPlayer.get(event.getPlayer());
        ApplicableRegions set =  rm.getRegionSet(event.getBlock());

        if (player.isRegionCreative() && !set.allows(Flags.CREATIVE, player)) { 
            event.getPlayer().sendMessage(L("blocked.outside_creative_break"));
            event.setCancelled(true);
        } else if (player.getRaw().getGameMode() != GameMode.CREATIVE && set.allows(Flags.CREATIVE)) {
            plugin.spawnblock.block(event.getBlock(), player);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;
        LCPlayer player = LCPlayer.get(event.getPlayer());
        if (player.isRegionCreative() && rm.isDiffrentRegion(event.getPlayer(), event.getBlock().getLocation())) {
            // do not build outside of creative regions, when in the region
            ApplicableRegions set =  rm.getRegionSet(event.getBlock());
            if (!set.allows(Flags.CREATIVE, player)) { 
                event.getPlayer().sendMessage(L("blocked.outside_creative"));
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerChangedArea(PlayerChangedAreaEvent event) {
        LCPlayer.get(event.getPlayer()).setRegionCreativeAllowed(event.getNewRegionSet().allows(Flags.CREATIVE), event.getMoveEvent());
    }
    
    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event.isCancelled())
            return;
        
        Block source = event.getBlock().getRelative(event.getDirection());
        LimitedCreativeCore.debug("PistonExtend "+source.getType()+" "+event.getDirection());
        if (source.getType() != Material.AIR) {
            if (regionSet(source).allows(Flags.CREATIVE)) {
                for (int i = 1; i <= 12; i++) {
                    Block dest = source.getRelative(event.getDirection(), i);
                    LimitedCreativeCore.debug("dest "+i+": "+dest.getType());
                    if (!regionSet(dest).allows(Flags.CREATIVE)) {
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
        LimitedCreativeCore.debug("PistonRetract "+source.getType()+" "+event.getDirection() + " " + event.isSticky());
        if (event.isSticky() && source.getType() != Material.AIR) { 
            LimitedCreativeCore.debug("dest "+dest.getType());
            if (regionSet(source).allows(Flags.CREATIVE)) {
                if (!regionSet(dest).allows(Flags.CREATIVE)) {
                    plugin.logger.warning(L("blocked.piston", source.getType().toString(), Util.toString(source.getLocation())));
                    event.setCancelled(true);
                }
            } else if (regionSet(dest).allows(Flags.CREATIVE)) {
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
