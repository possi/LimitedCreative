package de.jaschastarke.minecraft.limitedcreative.blockstate;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import de.jaschastarke.bukkit.lib.chat.ChatFormattings;
import de.jaschastarke.bukkit.lib.chat.InGameFormatter;
import de.jaschastarke.minecraft.limitedcreative.ModBlockStates;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockState.Source;

public class PlayerListener implements Listener {
    private ModBlockStates mod;
    public PlayerListener(ModBlockStates mod) {
        this.mod = mod;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block b = event.getClickedBlock();
            if (b != null && event.getPlayer().getInventory().getItemInMainHand().getType().equals(mod.getConfig().getTool()) && mod.getPlugin().getPermManager().hasPermission(event.getPlayer(), BlockStatePermissions.TOOL)) {
                if (mod.getConfig().getIgnoredWorlds().contains(event.getClickedBlock().getWorld().getName())) {
                    event.getPlayer().sendMessage(mod.getPlugin().getLocale().trans("command.blockstate.world_ignored", event.getClickedBlock().getWorld().getName()));
                } else {
                    showInfo(event.getPlayer(), b.getLocation(), b.getType());
                }
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Entity e = event.getRightClicked();
        if (e != null && e instanceof ItemFrame && event.getPlayer().getInventory().getItemInMainHand().getType().equals(mod.getConfig().getTool()) && mod.getPlugin().getPermManager().hasPermission(event.getPlayer(), BlockStatePermissions.TOOL)) {
            if (mod.getConfig().getIgnoredWorlds().contains(e.getWorld().getName())) {
                event.getPlayer().sendMessage(mod.getPlugin().getLocale().trans("command.blockstate.world_ignored", e.getWorld().getName()));
            } else {
                showInfo(event.getPlayer(), e.getLocation(), Material.ITEM_FRAME);
            }
            event.setCancelled(true);
        }
    }
    
    private void showInfo(Player pl, Location loc, Material type) {
        BlockState s = mod.getModel().getState(loc.getBlock());
        InGameFormatter f = new InGameFormatter(mod.getPlugin().getLang());
        String ret = null;
        if (s == null || s.getSource() == Source.UNKNOWN) {
            ret = f.formatString(ChatFormattings.ERROR, f.getString("block_state.tool_info.unknown", type.toString()));
        } else {
            String k = "block_state.tool_info." + s.getSource().name().toLowerCase();
            String gm = "";
            if (s.getGameMode() != null) {
                switch (s.getGameMode()) {
                    case CREATIVE:
                        gm = ChatColor.GOLD + s.getGameMode().toString().toLowerCase() + ChatColor.RESET;
                        break;
                    case SURVIVAL:
                        gm = ChatColor.GREEN + s.getGameMode().toString().toLowerCase() + ChatColor.RESET;
                        break;
                    case ADVENTURE:
                        gm = ChatColor.DARK_GREEN + s.getGameMode().toString().toLowerCase() + ChatColor.RESET;
                        break;
                    default:
                        break;
                }
            }
            
            ret = f.formatString(ChatFormattings.INFO, f.getString(k, type.toString(),
                                                                    s.getPlayerName(),
                                                                    gm,
                                                                    s.getDate()));
        }
        if (ret != null)
            pl.sendMessage(ret);
    }
}
