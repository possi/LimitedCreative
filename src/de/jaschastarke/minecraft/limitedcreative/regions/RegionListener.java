package de.jaschastarke.minecraft.limitedcreative.regions;

import static de.jaschastarke.minecraft.utils.Locale.L;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;

import de.jaschastarke.minecraft.limitedcreative.LCPlayer;
import de.jaschastarke.minecraft.limitedcreative.LimitedCreativeCore;
import de.jaschastarke.minecraft.utils.Util;
import de.jaschastarke.minecraft.worldguard.ApplicableRegions;
import de.jaschastarke.minecraft.worldguard.CRegionManager;

public class RegionListener implements Listener {
    private static LimitedCreativeCore plugin = WorldGuardIntegration.plugin;
    private static WorldGuardPlugin wg = WorldGuardIntegration.wg;
    private CRegionManager rm;
    public RegionListener(WorldGuardIntegration wgi) {
        rm = wgi.getRegionManager();
    }

    private ApplicableRegions regionSet(World world, Location loc) {
        RegionManager mgr = wg.getGlobalRegionManager().get(world);
        return new ApplicableRegions(mgr.getApplicableRegions(loc), rm.world(world));
    }
    private ApplicableRegions regionSet(Location loc) {
        return regionSet(loc.getWorld(), loc);
    }
    private ApplicableRegions regionSet(Block block) {
        return regionSet(block.getWorld(), block.getLocation());
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        LCPlayer player = LCPlayer.get(event.getPlayer());
        RegionManager mgr = wg.getGlobalRegionManager().get(event.getPlayer().getWorld());
        Vector pt = new Vector(event.getBlock().getLocation().getBlockX(), event.getBlock().getLocation().getBlockY(), event.getBlock().getLocation().getBlockZ());
        ApplicableRegions set = new ApplicableRegions(mgr.getApplicableRegions(pt), rm.world(event.getPlayer().getWorld()));

        if (player.isRegionCreative() && !set.allows(Flags.CREATIVE, player)) { 
            event.getPlayer().sendMessage(L("blocked.outside_creative_break"));
            event.setCancelled(true);
        } else if (set.allows(Flags.CREATIVE) && player.getRaw().getGameMode() != GameMode.CREATIVE) {
            plugin.spawnblock.block(event.getBlock(), player);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;
        LCPlayer player = LCPlayer.get(event.getPlayer());
        if (player.isRegionCreative()) {
            // do not build outside of creative regions, when in the region
            RegionManager mgr = wg.getGlobalRegionManager().get(event.getPlayer().getWorld());
            Vector pt = new Vector(event.getBlock().getLocation().getBlockX(), event.getBlock().getLocation().getBlockY(), event.getBlock().getLocation().getBlockZ());
            ApplicableRegions set = new ApplicableRegions(mgr.getApplicableRegions(pt), rm.world(event.getPlayer().getWorld()));
            if (!set.allows(Flags.CREATIVE, player)) { 
                event.getPlayer().sendMessage(L("blocked.outside_creative"));
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled())
            return;
        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {

            LCPlayer player = LCPlayer.get(event.getPlayer());
            RegionManager mgr = wg.getGlobalRegionManager().get(event.getPlayer().getWorld());
            ApplicableRegionSet applicableRegions = mgr.getApplicableRegions(event.getTo());
            LimitedCreativeCore.debug(applicableRegions.toString());
            ApplicableRegions set = new ApplicableRegions(applicableRegions, rm.world(event.getPlayer().getWorld()));
            
            player.setRegionCreativeAllowed(set.allows(Flags.CREATIVE, player), event);
        }
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled())
            return;
        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {

            LCPlayer player = LCPlayer.get(event.getPlayer());
            RegionManager mgr = wg.getGlobalRegionManager().get(event.getPlayer().getWorld());
            Vector pt = new Vector(event.getTo().getBlockX(), event.getTo().getBlockY(), event.getTo().getBlockZ());
            ApplicableRegions set = new ApplicableRegions(mgr.getApplicableRegions(pt), rm.world(event.getPlayer().getWorld()));
            
            player.setRegionCreativeAllowed(set.allows(Flags.CREATIVE, player), event);
        }
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
