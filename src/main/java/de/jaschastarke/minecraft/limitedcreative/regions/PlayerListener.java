package de.jaschastarke.minecraft.limitedcreative.regions;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;

import de.jaschastarke.minecraft.limitedcreative.Hooks;
import de.jaschastarke.minecraft.limitedcreative.ModRegions;

public class PlayerListener extends Listener {
    public PlayerListener(ModRegions mod) {
        super(mod);
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        
        Block block = event.getClickedBlock();
        
        if (block.getState() instanceof InventoryHolder || block.getType() == Material.ENDER_CHEST || // Workaround, Bukkit not recognize a Enderchest
                block.getState() instanceof Sign ||
                block.getState() instanceof Lever || block.getState() instanceof Button ||
                block.getType() == Material.WORKBENCH || block.getType() == Material.ANVIL) {

            PlayerMeta pmeta = new PlayerMeta(event.getPlayer());
            boolean diffrent_region = getRM().isDiffrentRegion(event.getPlayer(), block.getLocation());
            
            if (pmeta.isActiveRegionGameMode() && diffrent_region) {
                // do not break outside of "gamemod-change-region" when in the region
                if (getRM().getRegionSet(block).getFlag(Flags.GAMEMODE, event.getPlayer()) != pmeta.getActiveRegionGameMode()) {
                    event.getPlayer().sendMessage(L("blocked.outside_interact"));
                    event.setCancelled(true);
                }
            } else if (diffrent_region) {
                // do not break inside of "survial-region in creative world" when outside
                if (getRM().getRegionSet(block).getFlag(Flags.GAMEMODE, event.getPlayer()) != event.getPlayer().getGameMode()) {
                    event.getPlayer().sendMessage(L("blocked.inside_interact"));
                    event.setCancelled(true);
                }
            }
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        PlayerMeta pmeta = new PlayerMeta(event.getPlayer());
        Location loc = event.getRightClicked().getLocation();
        boolean diffrent_region = getRM().isDiffrentRegion(event.getPlayer(), loc);
        
        if (pmeta.isActiveRegionGameMode() && diffrent_region) {
            // do not break outside of "gamemod-change-region" when in the region
            if (getRM().getRegionSet(loc).getFlag(Flags.GAMEMODE, event.getPlayer()) != pmeta.getActiveRegionGameMode()) {
                event.getPlayer().sendMessage(L("blocked.outside_interact_entity"));
                event.setCancelled(true);
            }
        } else if (diffrent_region) {
            // do not break inside of "survial-region in creative world" when outside
            if (getRM().getRegionSet(loc).getFlag(Flags.GAMEMODE, event.getPlayer()) != event.getPlayer().getGameMode()) {
                event.getPlayer().sendMessage(L("blocked.inside_interact_entity"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamange(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            PlayerMeta pmeta = new PlayerMeta((Player) event.getDamager());
            Location loc = event.getEntity().getLocation();
            boolean diffrent_region = getRM().isDiffrentRegion((Player) event.getDamager(), loc);

            if (pmeta.isActiveRegionGameMode() && diffrent_region) {
                // do not break outside of "gamemod-change-region" when in the region
                if (getRM().getRegionSet(loc).getFlag(Flags.GAMEMODE, (Player) event.getDamager()) != pmeta.getActiveRegionGameMode()) {
                    event.getDamager().sendMessage(L("blocked.outside_interact_entity"));
                    event.setCancelled(true);
                }
            } else if (diffrent_region) {
                // do not break inside of "survial-region in creative world" when outside
                if (getRM().getRegionSet(loc).getFlag(Flags.GAMEMODE, (Player) event.getDamager()) != ((Player) event.getDamager()).getGameMode()) {
                    event.getDamager().sendMessage(L("blocked.inside_interact_entity"));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        onPlayerInteractEntity(event);
    }
    
    private boolean isRegionOptional(Player player) {
        return mod.getRegionManager().getRegionSet(player.getLocation()).allows(Flags.GAMEMODE_OPTIONAL);
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        PlayerMeta pmeta = new PlayerMeta(player);
        GameMode gm = event.getNewGameMode();
        
        if (mod.isDebug())
            mod.getLog().debug(player.getName() + " is changing to GameMode " + gm);
        if (Hooks.IsLoggedIn.test(player)) { // if authme is changing GameMode before going to teleport, this should be remembered
            if (pmeta.isActiveRegionGameMode()) { // change to the other gamemode as the area defines
                if (!pmeta.isActiveRegionGameMode(gm)) { // only when we are not switching to the mode the region allows
                    if (!mod.getPlugin().getPermManager().hasPermission(player, RegionPermissions.BYPASS) && !isRegionOptional(player)) {
                        player.sendMessage(ChatColor.RED + L("exception.region.not_optional", gm.toString().toLowerCase()));
                        mod.getLog().debug("... denied");
                        event.setCancelled(true);
                    } else {
                        pmeta.setOptionalRegionGameMode(gm);
                    }
                } else {
                    // we are changing to the mode the region defines, thats not permanent
                    pmeta.setOptionalRegionGameMode(null);
                    pmeta.setPermanentGameMode(null);
                }
            } else if (mod.getRegionManager().getRegionSet(player.getLocation()).getFlag(Flags.GAMEMODE, player) == null) {
                pmeta.setPermanentGameMode(gm); // we are not in a region, so the mode change is permanent
            }
        }
    }
}
