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

import static de.jaschastarke.minecraft.utils.Locale.L;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.jaschastarke.minecraft.utils.IPermission;
import de.jaschastarke.minecraft.utils.Util;

public class Commands {
    private static Core plugin;
    public static class MainCommandExecutor implements CommandExecutor {
        
        public enum Action {
            C, CREATIVE,
            S, SURVIVAL,
            A, ADVENTURE,
            E, ENABLE,
            D, DISABLE,
            R, REGION,
            RELOAD
        };
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            Core.debug(sender.getName() + ": /" + label + " " + Util.join(args));
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
                            case A:
                            case ADVENTURE:
                                this.setGameMode(GameMode.ADVENTURE, sender, args);
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
                            case RELOAD:
                                //plugin.getServer().getPluginManager().disablePlugin(plugin); // disable removes classloader, so re-enabling causes problems
                                //plugin.getServer().getPluginManager().enablePlugin(plugin);
                                plugin.reload();
                                return true;
                        }
                    } catch (CommandException e) {
                        Core.debug("CommandException: "+e.getMessage());
                        sender.sendMessage(ChatColor.DARK_RED + e.getLocalizedMessage());
                        return true;
                    }
                }
            }
            
            String c = label;
            StringBuilder message = new StringBuilder();
            message.append("/"+c+" s[urvival] ["+L("command.player")+"] - "+L("command.switch.survival")+"\n");
            message.append("/"+c+" c[reative] ["+L("command.player")+"] - "+L("command.switch.creative")+"\n");
            message.append("/"+c+" a[dventure] ["+L("command.player")+"] - "+L("command.switch.adventure")+"\n");
            if (hasPermission(sender, Perms.CONFIG)) {
                message.append("/"+c+" e[nable] "+L("command.config.overview")+"\n");
                message.append("/"+c+" d[isable] "+L("command.config.overview")+"\n");
                message.append("/"+c+" reload "+L("command.config.reload")+"\n");
            }
            if (hasPermission(sender, Perms.REGIONS))
                message.append("/"+c+" r[egion] "+L("command.worldguard.alias")+"\n");
            if (message.length() > 0) {
                sender.sendMessage("Usage:");
                for (String m : message.toString().split("\n")) {
                    sender.sendMessage(m);
                }
                return true;
            }
            return false;
        }
        
        private void setOption(CommandSender sender, String[] args, boolean b) throws CommandException {
            if (sender instanceof Player && !hasPermission(sender, Perms.CONFIG)) {
                throw new LackingPermissionException();
            }
            if (args.length > 2)
                throw new InvalidCommandException("exception.command.tomuchparameter");
            if (args.length < 2) {
                StringBuilder str = new StringBuilder(L("command.config.settings"));
                List<Configuration.Option> options = Configuration.Option.getAvailableOptions();
                for (int i = 0; i < options.size(); i++) {
                    str.append(options.get(i).name().toLowerCase());
                    if (i < options.size() - 1)
                        str.append(", ");
                    if ((i - 1) % 4 == 0) {
                        sender.sendMessage(str.toString());
                        str = new StringBuilder();
                    }
                }
                if (str.length() > 0)
                    sender.sendMessage(str.toString());
                return;
            }
            
            Configuration.Option opt = null;
            try {
                opt = Configuration.Option.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidCommandException("exception.command.invalidoption");
            }
            
            plugin.config.set(opt, b);
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

            if (target == null) {
                throw new InvalidCommandException("exception.command.playernotfound");
            } else if (sender instanceof Player && sender != target && !hasPermission(sender, Perms.GM_OTHER)) {
                throw new LackingPermissionException();
            } else if (target.getGameMode() != gm) {
                if ((hasPermission(sender, Perms.GM)) ||
                    Players.get(target).isGameModeAllowed(gm) ||
                    (gm == plugin.com.getDefaultGameMode(target.getWorld()) && hasPermission(sender, Perms.GM_BACKONLY)) ||
                    (gm == GameMode.CREATIVE && hasPermission(sender, Perms.GM_CREATIVE)) ||
                    (gm == GameMode.SURVIVAL && hasPermission(sender, Perms.GM_SURVIVAL)) ||
                    (gm == GameMode.ADVENTURE && hasPermission(sender, Perms.GM_ADVENTURE))) {
                    target.setGameMode(gm);
                } else {
                    throw new LackingPermissionException();
                }
                if (target != sender) {
                    sender.sendMessage(L("command.gamemode.changed", target.getName()));
                }
            } else {
                sender.sendMessage(L("command.gamemode.no_change"));
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
    
    public static void register(Core pplugin) {
        plugin = pplugin;
        plugin.getCommand("limitedcreative").setExecutor(new MainCommandExecutor());
        if (plugin.worldguard == null) {
            plugin.getCommand("/region").setExecutor(new NotAvailableCommandExecutor());
        }
    }
    private static boolean hasPermission(CommandSender sender, IPermission permission) {
        return plugin.perm.hasPermission(sender, permission);
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
