package de.jaschastarke.minecraft.limitedcreative.regions;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import de.jaschastarke.bukkit.lib.Utils;
import de.jaschastarke.minecraft.limitedcreative.Hooks;
import de.jaschastarke.minecraft.limitedcreative.ModRegions;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.ApplicableRegions;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.CustomRegionManager;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.events.PlayerAreaEvent;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.events.PlayerChangedAreaEvent;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.events.PlayerNewLocationAreaEvent;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.events.PlayerSetAreaEvent;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.events.PlayerUpdateAreaEvent;

public class RegionListener implements Listener {
    private ModRegions mod;
    private CustomRegionManager rm;
    
    public RegionListener(ModRegions mod) {
        this.mod = mod;
        rm = mod.getRegionManager();
    }
    
    /**
     * The isCancelled in PlayerInteractEvent doesn't check useItemInHand, even this decides (when clicking on
     * entity with e.g. a bucket)
     * @param event
     * @return The relevant "isCancelled"
     */
    public static boolean isCancelled(PlayerInteractEvent event) {
        return event.useInteractedBlock() == Event.Result.DENY && event.useItemInHand() == Event.Result.DENY;
    }

    private ApplicableRegions regionSet(Location loc) {
        return rm.getRegionSet(loc);
    }

    public int getFloatingHeight(Player player) {
        return getFloatingHeight(player.getLocation());
    }
    public int getFloatingHeight(Location loc) {
        Block b = loc.getBlock();
        int steps = 0;
        while (b.getType() == Material.AIR) {
            steps++;
            b = b.getRelative(BlockFace.DOWN);
        }
        return steps;
    }
    
    private boolean checkSwitchFlight(PlayerMoveEvent event) {
        if (event != null && event.getPlayer().getGameMode() == GameMode.CREATIVE && getFloatingHeight(event.getTo()) > mod.getConfig().getMaximumFloatingHeight()) {
            // but not if he is too high
            Utils.sendTimeoutMessage(event.getPlayer(), L("blocked.survival_flying"));
            
            Location newloc = event.getTo().clone();
            newloc.setX(event.getFrom().getX());
            newloc.setY(event.getFrom().getY()); // well, otherwise flying high out of the region is possible
            newloc.setZ(event.getFrom().getZ());
            event.setTo(newloc);
            return false;
        }
        return true;
    }
    private boolean checkSwitchFlight(PlayerAreaEvent area_event) {
        if (area_event instanceof PlayerChangedAreaEvent) {
            if (!checkSwitchFlight(((PlayerChangedAreaEvent) area_event).getMoveEvent())) {
                ((PlayerChangedAreaEvent) area_event).setCancelled(true);
                return false;
            }
        }
        return true;
    }
    private void setRegionGameMode(GameMode region_gamemode, PlayerAreaEvent area_event) {
        Player player = area_event.getPlayer();
        PlayerData.Data pdata = mod.getPlayerData(player);
        if (mod.isDebug())
            mod.getLog().debug(player.getName()+": changed region: "+region_gamemode+": " + area_event);
        
        PlayerMoveEvent event = null;
        if (area_event instanceof PlayerChangedAreaEvent)
            event = ((PlayerChangedAreaEvent) area_event).getMoveEvent();
        GameMode CURRENT_GAMEMODE = player.getGameMode();
        GameMode DEFAULT_GAMEMODE = Hooks.DefaultWorldGameMode.get(event != null ? event.getTo().getWorld() : player.getWorld());
        
        if (region_gamemode != null && CURRENT_GAMEMODE != region_gamemode && !pdata.isActiveRegionGameMode(region_gamemode)) {
            if (mod.isDebug())
                mod.getLog().debug(player.getName()+": entering creative area");
            // 1. the region allows "the other (temporary) gamemode"
            // 2. but the player is not in that mode
            // 3. and the player is not aware of that
            // result: change him to that mode
            
            boolean isOptional = pdata.getOptionalRegionGameMode(area_event.getRegionHash()) == CURRENT_GAMEMODE;
            
            if (isOptional || checkSwitchFlight(area_event)) {
                pdata.storeActiveRegionGameMode(region_gamemode); // have to be set, before setGameMode
                
                if (!isOptional) {
                    player.setGameMode(region_gamemode);
                }
            }
        } else if (region_gamemode == null && player.getGameMode() != DEFAULT_GAMEMODE && !pdata.isInPermanentGameMode(CURRENT_GAMEMODE) && pdata.getActiveRegionGameMode() != null) {
            if (mod.isDebug())
                mod.getLog().debug(player.getName()+": leaving creative area");
            // 1. the region doesn't allow "the other gamemode"
            // 2. but the player is in that mode
            // 3. and the player isn't global (permanent) in that mode
            // 4. and the player isn't temporary in that mode (that means its maybe a world teleport, and the mode changes afterwards)
            // result: change him back to default mode
            if (checkSwitchFlight(area_event)) {
                pdata.storeActiveRegionGameMode(null);
                player.setGameMode(DEFAULT_GAMEMODE);
            }
        } else if (region_gamemode == null && pdata.isActiveRegionGameMode()) {
            if (mod.isDebug())
                mod.getLog().debug(player.getName()+": leaving creative area (while already in default gamemode)");
            // 1. the region doesn't allow "the other gamemode"
            // 2. but he thinks he is still allowed
            // 3. (because of else) we are not longer in that mode
            // result: advise him to not longer allowed to that region
            pdata.storeActiveRegionGameMode(null);
        } else if (region_gamemode == CURRENT_GAMEMODE && !pdata.isInPermanentGameMode(CURRENT_GAMEMODE)) {
            if (!pdata.isActiveRegionGameMode(region_gamemode)) {
                // we have no information why we are already in this gamemode, so this may be because of an AuthMe change-and-teleport
                pdata.storeActiveRegionGameMode(region_gamemode);
            }
        }
    }
    
    @EventHandler
    public void onPlayerChangedArea(PlayerNewLocationAreaEvent event) {
        setRegionGameMode(event.getRegionSet().getFlag(Flags.GAMEMODE, event.getPlayer()), event);
    }
    
    @EventHandler
    public void onPlayerSetArea(PlayerSetAreaEvent event) {
        setRegionGameMode(event.getRegionSet().getFlag(Flags.GAMEMODE, event.getPlayer()), event);
    }
    
    @EventHandler
    public void onPlayerUpdateArea(PlayerUpdateAreaEvent event) {
        setRegionGameMode(event.getRegionSet().getFlag(Flags.GAMEMODE, event.getPlayer()), event);
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
    
    private String L(String msg, Object... args) {
        return mod.getPlugin().getLocale().trans(msg, args);
    }
}
