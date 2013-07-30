package de.jaschastarke.minecraft.limitedcreative.regions;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import de.jaschastarke.bukkit.lib.Utils;
import de.jaschastarke.minecraft.limitedcreative.Hooks;
import de.jaschastarke.minecraft.limitedcreative.ModRegions;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.events.PlayerAreaEvent;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.events.PlayerChangedAreaEvent;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.events.PlayerNewLocationAreaEvent;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.events.PlayerSetAreaEvent;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.events.PlayerUpdateAreaEvent;

public class RegionListener extends Listener {
    public RegionListener(ModRegions mod) {
        super(mod);
    }
    
    private boolean checkFloatingToHigh(int max, Location loc) {
        if (max < 0)
            return false;
        return getFloatingHeight(loc) > max;
    }
    public int getFloatingHeight(Player player) {
        return getFloatingHeight(player.getLocation());
    }
    public int getFloatingHeight(Location loc) {
        Block b = loc.getBlock().getRelative(BlockFace.DOWN);
        int steps = 0;
        while (b.getType() == Material.AIR) {
            steps++;
            b = b.getRelative(BlockFace.DOWN);
        }
        return steps;
    }
    
    private boolean checkSwitchFlight(PlayerMoveEvent event) {
        if (event != null && event.getPlayer().getGameMode() == GameMode.CREATIVE && checkFloatingToHigh(mod.getConfig().getMaxFallingHeight(), event.getTo())) {
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
    private boolean checkSwitchFlight(PlayerAreaEvent areaEvent) {
        if (areaEvent instanceof PlayerChangedAreaEvent) {
            if (!checkSwitchFlight(((PlayerChangedAreaEvent) areaEvent).getMoveEvent())) {
                ((PlayerChangedAreaEvent) areaEvent).setCancelled(true);
                return false;
            }
        }
        return true;
    }
    private void setRegionGameMode(GameMode regionGameMode, PlayerAreaEvent areaEvent) {
        Player player = areaEvent.getPlayer();
        PlayerMeta pmeta = new PlayerMeta(player);
        
        if (mod.isDebug())
            mod.getLog().debug(player.getName()+": changed region: "+regionGameMode+": " + areaEvent);
        
        PlayerMoveEvent moveEvent = null;
        if (areaEvent instanceof PlayerChangedAreaEvent)
            moveEvent = ((PlayerChangedAreaEvent) areaEvent).getMoveEvent();
        GameMode currentGameMode = player.getGameMode();
        GameMode defaultGameMode = Hooks.DefaultWorldGameMode.get(moveEvent != null ? moveEvent.getTo().getWorld() : player.getWorld());
        
        if (regionGameMode != null && currentGameMode != regionGameMode && !pmeta.isActiveRegionGameMode(regionGameMode)) {
            if (mod.isDebug())
                mod.getLog().debug(player.getName()+": entering creative area");
            // 1. the region allows "the other (temporary) gamemode"
            // 2. but the player is not in that mode
            // 3. and the player is not aware of that
            // result: change him to that mode
            
            boolean isOptional = pmeta.getOptionalRegionGameMode(areaEvent.getRegionHash()) == currentGameMode;
            
            if (isOptional || checkSwitchFlight(areaEvent)) {
                pmeta.storeActiveRegionGameMode(regionGameMode); // have to be set, before setGameMode
                
                if (!isOptional) {
                    player.setGameMode(regionGameMode);
                }
            }
        } else if (regionGameMode == null && player.getGameMode() != defaultGameMode && !pmeta.isInPermanentGameMode(currentGameMode) && pmeta.getActiveRegionGameMode() != null) {
            if (mod.isDebug())
                mod.getLog().debug(player.getName()+": leaving creative area");
            // 1. the region doesn't allow "the other gamemode"
            // 2. but the player is in that mode
            // 3. and the player isn't global (permanent) in that mode
            // 4. and the player isn't temporary in that mode (that means its maybe a world teleport, and the mode changes afterwards)
            // result: change him back to default mode
            if (checkSwitchFlight(areaEvent)) {
                pmeta.storeActiveRegionGameMode(null);
                player.setGameMode(defaultGameMode);
            }
        } else if (regionGameMode == null && pmeta.isActiveRegionGameMode()) {
            if (mod.isDebug())
                mod.getLog().debug(player.getName()+": leaving creative area (while already in default gamemode)");
            // 1. the region doesn't allow "the other gamemode"
            // 2. but he thinks he is still allowed
            // 3. (because of else) we are not longer in that mode
            // result: advise him to not longer allowed to that region
            pmeta.storeActiveRegionGameMode(null);
        } else if (regionGameMode == currentGameMode && !pmeta.isInPermanentGameMode(currentGameMode)) {
            if (!pmeta.isActiveRegionGameMode(regionGameMode)) {
                // we have no information why we are already in this gamemode, so this may be because of an AuthMe change-and-teleport
                pmeta.storeActiveRegionGameMode(regionGameMode);
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
}
