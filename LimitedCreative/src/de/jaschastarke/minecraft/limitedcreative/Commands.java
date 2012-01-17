package de.jaschastarke.minecraft.limitedcreative;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
//import org.bukkit.plugin.PluginManager;

public class Commands {
    private static LimitedCreativeCore plugin;
    //private static PluginManager pm;

    public static class MainCommandExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            Player player = null;
            if (sender instanceof Player) {
                player = (Player) sender;
            } else {
                sender.sendMessage("Console-Commands not supported");
                return true;
            }
            
            if (args.length >= 1 && args[0].equalsIgnoreCase("load")) {
                if (args.length == 1 || args[1].equalsIgnoreCase("survival") || args[1] == "0") {
                    new Inventory(player).load(GameMode.SURVIVAL);
                    return true;
                }/* else if (args.length == 2 && (args[1].equalsIgnoreCase("creative") || args[1] == "1")) {
                    new Inventory(player).load(GameMode.CREATIVE);
                    return true;
                }*/
            }
            return false;
        }
        
    }
    
    public static void register(LimitedCreativeCore pplugin) {
        plugin = pplugin;
        //pm = plugin.getServer().getPluginManager();
        
        plugin.getCommand("limitedcreative").setExecutor(new MainCommandExecutor());
    }
}
