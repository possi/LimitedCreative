/*
 * Limited Creative - (Bukkit Plugin)
 * Copyright (C) 2012 jascha@ja-s.de
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.jaschastarke.minecraft.limitedcreative;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.jaschastarke.minecraft.worldguard.CCommand;
import static de.jaschastarke.minecraft.utils.Locale.L;

public class Commands {
    private static LimitedCreativeCore plugin;
    public static class MainCommandExecutor implements CommandExecutor {
        
        public enum Action {
            C, CREATIVE,
            S, SURVIVAL,
            E, ENABLE,
            D, DISABLE,
            R, REGION,
        };
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length > 0) {
                Action act = null;
                try {
                    act = Action.valueOf(args[0].toUpperCase());
                } catch (IllegalArgumentException e) {}
                if (act != null) {
                    try {
                        switch (act) {
                            case C:
                            case CREATIVE:
                                this.setGameMode(GameMode.CREATIVE, sender, args);
                                return true;
                            case S:
                            case SURVIVAL:
                                this.setGameMode(GameMode.SURVIVAL, sender, args);
                                return true;
                            case E:
                            case ENABLE:
                                this.setOption(sender, args, true);
                                return true;
                            case D:
                            case DISABLE:
                                this.setOption(sender, args, false);
                                return true;
                            case R:
                            case REGION:
                                args = Arrays.copyOfRange(args, 1, args.length);
                                plugin.getCommand("/region").execute(sender, "/region", args);
                                return true;
                        }
                    } catch (CommandException e) {
                        sender.sendMessage(ChatColor.DARK_RED + e.getLocalizedMessage());
                        return true;
                    }
                }
            }
            
            String c = label;
            String message = "";
            if (sender.hasPermission("limitedcreative.switch_gamemode") || sender.hasPermission("limitedcreative.switch_gamemode.backonly"))
                message += "/"+c+" s[urvival] ["+L("command.player")+"] - "+L("command.switch.survival")+"\n";
            if (sender.hasPermission("limitedcreative.switch_gamemode"))
                message += "/"+c+" c[reative] ["+L("command.player")+"] - "+L("command.switch.creative")+"\n";
            if (sender.hasPermission("limitedcreative.config"))
                message += "/"+c+" e[nable] "+L("command.config.overview")+"\n";
            if (sender.hasPermission("limitedcreative.config"))
                message += "/"+c+" d[isable] "+L("command.config.overview")+"\n";
            if (sender.hasPermission("limitedcreative.config"))
                message += "/"+c+" r[egion] "+L("command.worldguard.alias")+"\n";
            if (message.length() > 0) {
                sender.sendMessage("Usage:");
                for (String m : message.split("\n")) {
                    sender.sendMessage(m);
                }
                return true;
            }
            return false;
        }


        public enum Option {
            STORECREATIVE,
            BLOCKPICKUP,
            BLOCKSIGN,
            PERMISSIONS,
            PERM_KEEPINVENTORY,
        };
        
        private void setOption(CommandSender sender, String[] args, boolean b) throws CommandException {
            if (sender instanceof Player && !sender.hasPermission("limitedcreative.config") && !sender.isOp()) {
                throw new LackingPermissionException();
            }
            if (args.length > 2)
                throw new InvalidCommandException("exception.command.tomuchparameter");
            if (args.length < 2)
                throw new InvalidCommandException("exception.command.missingparameter");
            
            Option opt = null;
            try {
                opt = Option.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidCommandException("exception.command.invalidoption");
            }
            
            switch (opt) {
                case STORECREATIVE:
                    plugin.config.setStoreCreative(b);
                    break;
                case BLOCKPICKUP:
                    plugin.config.setBlockPickupInCreative(b);
                    break;
                case BLOCKSIGN:
                    plugin.config.setSignBlock(b);
                    break;
                case PERMISSIONS:
                    plugin.config.setPermissionsEnabled(b);
                    break;
                case PERM_KEEPINVENTORY:
                    plugin.config.setPermissionToKeepInventory(b);
                    break;
            }
            sender.sendMessage(L("command.option.done"));
        }

        private void setGameMode(GameMode gm, CommandSender sender, String[] args) throws CommandException {
            Player target = null;
            if (args.length > 2)
                throw new InvalidCommandException("exception.command.tomuchparameter");
            if (args.length == 2)
                target = plugin.getServer().getPlayer(args[1]);
            else if (sender instanceof Player)
                target = (Player) sender;
            
            if (sender instanceof Player && !sender.isOp()) {
                if (!sender.hasPermission("limitedcreative.switch_gamemode")) {
                    if (gm != GameMode.SURVIVAL || !sender.hasPermission("limitedcreative.switch_gamemode.backonly")) {
                        throw new LackingPermissionException();
                    }
                }
                if (sender != target && !sender.hasPermission("limitedcreative.switch_gamemode.other")) {
                    throw new LackingPermissionException();
                }
            }
            if (target == null) {
                throw new InvalidCommandException("exception.command.playernotfound");
            }
            if (target.getGameMode() != gm) {
                target.setGameMode(gm);
                if (target != sender) {
                    sender.sendMessage(L("commands.gamemode.changed", target.getName()));
                }
            }
        }
        
    }
    
    public static class NotAvailableCommandExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            sender.sendMessage(ChatColor.DARK_RED + L("command.worldguard.no_integration"));
            return true;
        }
        
    }
    
    public static void register(LimitedCreativeCore pplugin) {
        plugin = pplugin;
        plugin.getCommand("limitedcreative").setExecutor(new MainCommandExecutor());
        if (plugin.worldguard != null) {
            plugin.getCommand("/region").setExecutor(new CCommand(plugin, plugin.worldguard.getRegionManager()));
        } else {
            plugin.getCommand("/region").setExecutor(new NotAvailableCommandExecutor());
        }
    }
    
    abstract static public class CommandException extends Exception {
        private static final long serialVersionUID = 1L;

        public CommandException(String s) {super(s);}

        @Override
        public String getLocalizedMessage() {
            return L(super.getLocalizedMessage());
        }
    }
    public static class InvalidCommandException extends CommandException {
        private static final long serialVersionUID = 1L;

        public InvalidCommandException(String s) {super(s);}
    }
    public static class LackingPermissionException extends CommandException {
        private static final long serialVersionUID = 1L;

        public LackingPermissionException() {super("exception.command.lackingpermission");}
    }
}
