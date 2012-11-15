package de.jaschastarke.minecraft.limitedcreative.cmdblock;

import static de.jaschastarke.minecraft.utils.Locale.L;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import de.jaschastarke.minecraft.limitedcreative.Core;
import de.jaschastarke.minecraft.limitedcreative.LCPlayer;
import de.jaschastarke.minecraft.limitedcreative.Players;

public class CommandBlocker {
    private Core plugin;
    public CommandBlocker(Core plugin) {
        this.plugin = plugin;
        
        plugin.getServer().getPluginManager().registerEvents(new Listener(), plugin);
    }
    
    
    class Listener implements org.bukkit.event.Listener {
        @EventHandler
        public void onPreCommand(PlayerCommandPreprocessEvent event) {
            String cmd = event.getMessage();
            if (cmd.startsWith("/")) { // just to be sure ;)
                cmd = cmd.substring(1);
                for (ICmdBlockEntry blockentry : plugin.config.getCommandBlockList()) {
                    if (blockentry.test(cmd)) {
                        LCPlayer player = Players.get(event.getPlayer());
                        if (!player.hasPermission(CmdBlockPerms.ALL)) {
                            Core.debug("CmdBlock: "+event.getPlayer().getName()+": '/"+cmd+"' blocked by rule '"+blockentry.toString()+"'");
                            event.setCancelled(true);
                            event.getPlayer().sendMessage(L("cmdblock.blocked"));
                            return;
                        }
                    }
                }
            }
        }
    }
}
