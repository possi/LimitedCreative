package de.jaschastarke.minecraft.limitedcreative.blockstate;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import de.jaschastarke.bukkit.lib.chat.ChatFormattings;
import de.jaschastarke.bukkit.lib.chat.NullFormatter;
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
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block b = event.getClickedBlock();
            if (b != null && event.getPlayer().getItemInHand().getType().equals(mod.getConfig().getToolType())) {
                BlockState s = mod.getDB().find(BlockState.class, new BlockLocation(b.getLocation()));
                NullFormatter f = new NullFormatter();
                String ret = null;
                if (s == null || s.getSource() == Source.UNKNOWN) {
                    ret = f.formatString(ChatFormattings.ERROR, f.getString("block_state.tool_info.unknown"));
                } else {
                    String k = "block_state.tool_info." + s.getSource().name().toLowerCase();
                    ret = f.formatString(ChatFormattings.INFO, f.getString(k, b.getType(), s.getPlayerName(), s.getDate()));
                }
                if (ret != null)
                    event.getPlayer().sendMessage(ret);
            }
        }
    }
}
