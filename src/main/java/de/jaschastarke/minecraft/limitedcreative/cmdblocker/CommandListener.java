package de.jaschastarke.minecraft.limitedcreative.cmdblocker;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import de.jaschastarke.minecraft.limitedcreative.ModCmdBlocker;

public class CommandListener implements Listener {
    private ModCmdBlocker mod;
    public CommandListener(ModCmdBlocker mod) {
        this.mod = mod;
    }
    
    @EventHandler
    public void onPreCommand(PlayerCommandPreprocessEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            String cmd = event.getMessage();
            if (cmd.startsWith("/")) { // just to be sure ;)
                cmd = cmd.substring(1);
                for (ICmdBlockEntry blockentry : mod.getConfig().getCommandBlockList()) {
                    if (blockentry.test(cmd)) {
                        if (!mod.getPlugin().getPermManager().hasPermission(event.getPlayer(), CmdBlockPermissions.COMMAND(cmd))) {
                            if (mod.isDebug())
                                mod.getLog().debug("CmdBlock: " + event.getPlayer().getName() + ": '/" + cmd + "' blocked by rule '" + blockentry.toString() + "'");
                            event.setCancelled(true);
                            event.getPlayer().sendMessage(mod.getPlugin().getLocale().trans("cmdblock.blocked"));
                            return;
                        }
                    }
                }
            }
        }
    }
}
