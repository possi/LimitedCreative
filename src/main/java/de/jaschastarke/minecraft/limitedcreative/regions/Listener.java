package de.jaschastarke.minecraft.limitedcreative.regions;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import de.jaschastarke.minecraft.limitedcreative.ModRegions;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.ApplicableRegions;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.CustomRegionManager;

abstract public class Listener implements org.bukkit.event.Listener {
    protected ModRegions mod;
    protected CustomRegionManager rm;
    
    public Listener(ModRegions mod) {
        this.mod = mod;
        rm = mod.getRegionManager();
    }
    
    /**
     * The isCancelled in PlayerInteractEvent doesn't check useItemInHand, even this decides (when clicking on
     * entity with e.g. a bucket)
     * @param event
     * @return The relevant "isCancelled"
     */
    protected static boolean isCancelled(PlayerInteractEvent event) {
        return event.useInteractedBlock() == Event.Result.DENY && event.useItemInHand() == Event.Result.DENY;
    }

    protected ApplicableRegions regionSet(Location loc) {
        return rm.getRegionSet(loc);
    }
    protected ApplicableRegions regionSet(Block block) {
        return rm.getRegionSet(block);
    }
    
    class PlayerMeta {
        private Player player;
        private PlayerData.Data pdata;
        public PlayerMeta(Player player) {
            this.player = player;
            this.pdata = mod.getPlayerData(player);
        }
        
        // Active Region GameMode
        public GameMode getActiveRegionGameMode() {
            return pdata.getActiveRegionGameMode();
        }
        public boolean isActiveRegionGameMode() {
            return getActiveRegionGameMode() != null;
        }
        public boolean isActiveRegionGameMode(GameMode gm) {
            GameMode agm = getActiveRegionGameMode();
            return agm != null && agm.equals(gm);
        }
        public void storeActiveRegionGameMode(GameMode regionGameMode) {
            pdata.storeActiveRegionGameMode(regionGameMode);
        }
        
        // Optional GameMode
        public void setOptionalRegionGameMode(GameMode gm) {
            if (mod.getConfig().getRememberOptional())
                pdata.setOptionalRegionGameMode(getCurrentRegionHash(), gm);
            else
                pdata.setOptionalRegionGameMode(getCurrentRegionHash(), null);
        }
        private String getCurrentRegionHash() {
            return mod.getRegionManager().getRegionsHash(player.getLocation());
        }
        public GameMode getOptionalRegionGameMode(String regionHash) {
            if (!mod.getConfig().getRememberOptional())
                return null;
            return pdata.getOptionalRegionGameMode(regionHash);
        }
        
        // Permanent GameMode
        public void setPermanentGameMode(GameMode gm) {
            if (mod.getConfig().getSafeMode())
                pdata.storePermanentGameMode(null);
            else
                pdata.storePermanentGameMode(gm);
        }
        public boolean isInPermanentGameMode(GameMode currentGameMode) {
            if (mod.getConfig().getSafeMode())
                return false;
            return pdata.getPermanentRegionGameMode() != null;
        }
    }
    
    protected String L(String msg, Object... args) {
        return mod.getPlugin().getLocale().trans(msg, args);
    }
}
