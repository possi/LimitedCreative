package de.jaschastarke.minecraft.limitedcreative.regions;

import static de.jaschastarke.minecraft.utils.Locale.L;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;

import de.jaschastarke.minecraft.limitedcreative.LCPlayer;
import de.jaschastarke.minecraft.limitedcreative.LimitedCreativeCore;
import de.jaschastarke.minecraft.worldguard.ApplicableRegions;
import de.jaschastarke.minecraft.worldguard.CRegionManager;

public class RegionListener implements Listener {
    private static LimitedCreativeCore plugin = WorldGuardIntegration.plugin;
    private static WorldGuardPlugin wg = WorldGuardIntegration.wg;
    private CRegionManager rm;
    public RegionListener(WorldGuardIntegration wgi) {
        rm = wgi.getRegionManager();
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
            Vector pt = new Vector(event.getTo().getBlockX(), event.getTo().getBlockY(), event.getTo().getBlockZ());
            ApplicableRegions set = new ApplicableRegions(mgr.getApplicableRegions(pt), rm.world(event.getPlayer().getWorld()));
            
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
}
