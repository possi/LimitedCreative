package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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
        if (event.isCancelled())
            return;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && mod.getPlugin().getPermManager().hasPermission(event.getPlayer(), BlockStatePermissions.TOOL)) {
            Block b = event.getClickedBlock();
            if (b != null && event.getPlayer().getItemInHand().getType().equals(mod.getConfig().getTool())) {
                try {
                    BlockState s = mod.getQueries().find(b.getLocation());
                    InGameFormatter f = new InGameFormatter(mod.getPlugin().getLang());
                    String ret = null;
                    if (s == null || s.getSource() == Source.UNKNOWN) {
                        ret = f.formatString(ChatFormattings.ERROR, f.getString("block_state.tool_info.unknown", b.getType().toString()));
                    } else {
                        String k = "block_state.tool_info." + s.getSource().name().toLowerCase();
                        String gm = s.getGameMode().toString().toLowerCase();
                        switch (s.getGameMode()) {
                            case CREATIVE:
                                gm = ChatColor.GOLD + gm + ChatColor.RESET;
                            case SURVIVAL:
                                gm = ChatColor.GREEN + gm + ChatColor.RESET;
                            case ADVENTURE:
                                gm = ChatColor.DARK_GREEN + gm + ChatColor.RESET;
                            default:
                                break;
                        }
                        
                        ret = f.formatString(ChatFormattings.INFO, f.getString(k, b.getType().toString(),
                                                                                s.getPlayerName(),
                                                                                gm,
                                                                                s.getDate()));
                    }
                    if (ret != null)
                        event.getPlayer().sendMessage(ret);
                } catch (SQLException e) {
                    mod.getLog().warn("DB-Error while onPlayerInteract: "+e.getMessage());
                }
            }
        }
    }
}
