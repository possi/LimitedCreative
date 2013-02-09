package de.jaschastarke.minecraft.limitedcreative.regions;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;

import de.jaschastarke.minecraft.limitedcreative.ModRegions;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.CustomRegionManager;

public class PlayerListener implements Listener {
    private ModRegions mod;
    private CustomRegionManager rm;
    
    public PlayerListener(ModRegions mod) {
        this.mod = mod;
        rm = mod.getRegionManager();
    }
    
    /**
     * The isCancelled in PlayerInteractEvent doesn't check useItemInHand, even this decides (when clicking on
     * entity with e.g. a bucket)
     * @param event
     * @return The relevant "isCancelled"
     */
    private static boolean isCancelled(PlayerInteractEvent event) {
        return event.useInteractedBlock() == Event.Result.DENY && event.useItemInHand() == Event.Result.DENY;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (isCancelled(event))
            return;
        
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        
        Block block = event.getClickedBlock();
        
        if (block.getState() instanceof InventoryHolder || block.getType() == Material.ENDER_CHEST || // Workaround, Bukkit not recognize a Enderchest
                block.getState() instanceof Sign ||
                block.getState() instanceof Lever || block.getState() instanceof Button ||
                block.getType() == Material.WORKBENCH || block.getType() == Material.ANVIL) {

            PlayerData.Data pdata = mod.getPlayerData(event.getPlayer());
            boolean diffrent_region = rm.isDiffrentRegion(event.getPlayer(), block.getLocation());
            
            if (pdata.isActiveRegionGameMode() && diffrent_region) {
                // do not break outside of "gamemod-change-region" when in the region
                if (rm.getRegionSet(block).getFlag(Flags.GAMEMODE, event.getPlayer()) != pdata.getActiveRegionGameMode()) {
                    event.getPlayer().sendMessage(L("blocked.outside_interact"));
                    event.setCancelled(true);
                }
            } else if (diffrent_region) {
                // do not break inside of "survial-region in creative world" when outside
                if (rm.getRegionSet(block).getFlag(Flags.GAMEMODE) != null) {
                    event.getPlayer().sendMessage(L("blocked.inside_interact"));
                    event.setCancelled(true);
                }
            }
        }
    }
    
    private String L(String msg, Object... args) {
        return mod.getPlugin().getLocale().trans(msg, args);
    }
}
