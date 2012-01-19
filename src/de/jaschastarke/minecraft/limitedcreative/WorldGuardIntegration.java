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

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

import static de.jaschastarke.minecraft.utils.Locale.L;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.jaschastarke.minecraft.regions.ApplicableRegions;
import de.jaschastarke.minecraft.regions.CRegion;
import de.jaschastarke.minecraft.regions.CRegionManager;
import de.jaschastarke.minecraft.regions.FlagList;
import de.jaschastarke.minecraft.regions.FlagValue;
import de.jaschastarke.minecraft.utils.Util;

public class WorldGuardIntegration {
    public static LimitedCreativeCore plugin;
    public static WorldGuardPlugin wg;
    private CRegionManager rm;

    public WorldGuardIntegration(LimitedCreativeCore pplugin) {
        plugin = pplugin;
        wg = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
    }
    
    public static boolean available() {
        return LimitedCreativeCore.plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null;
    }
    

    public static final StateFlag CREATIVE = new StateFlag("creative", false);
    public static final RegionGroupFlag CREATIVE_GROUP = new RegionGroupFlag("creative-group", RegionGroupFlag.RegionGroup.MEMBERS);
    static {
        CREATIVE.setGroupFlag(CREATIVE_GROUP);
    }
    
    public void init() {
        rm = new CRegionManager(new File(plugin.getDataFolder(), "regions.yml"));
        FlagList.addFlag(CREATIVE);
        FlagList.addFlag(CREATIVE_GROUP);
        
        new WGIPlayerListen().register();
    }

    public enum Action {
        FLAG,
        INFO
    }
    
    public class WGIPlayerListen extends PlayerListener {
        @Override
        public void onPlayerMove(PlayerMoveEvent event) {
            if (event.isCancelled())
                return;
            if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                    || event.getFrom().getBlockY() != event.getTo().getBlockY()
                    || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {

                LCPlayer player = LCPlayer.get(event.getPlayer());
                RegionManager mgr = wg.getGlobalRegionManager().get(event.getPlayer().getWorld());
                Vector pt = new Vector(event.getTo().getBlockX(), event.getTo().getBlockY(), event.getTo().getBlockZ());
                ApplicableRegions set = new ApplicableRegions(mgr.getApplicableRegions(pt), rm.world(event.getPlayer().getWorld()));
                
                player.setRegionCreativeAllowed(set.allows(CREATIVE, player), event);
            }
        }
        
        private void register() {
            plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, this, Priority.Normal, plugin);
        }
    }
    
    /**
     * @TODO: Move "generic" region command to .regions-NS
     */
    public class WGICommandExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length < 2) {
                return false;
            }
            if (!sender.hasPermission("limitedcreative.regions")) {
                sender.sendMessage(ChatColor.DARK_RED + "exception.command.lackingpermission");
                return true;
            }
            Player player = sender instanceof Player ? (Player) sender : null;
            World world = null;
            Action act;
            String rid;
            try {
                act = Action.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                return false;
            }
            String[] wr = args[1].split("#");
            if (wr.length == 2) {
                world = plugin.getServer().getWorld(wr[0]);
                rid = wr[1];
            } else {
                rid = args[1];
                if (player != null) {
                    world = player.getWorld();
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + L("command.worldguard.world_not_found"));
                    return true;
                }
            }
            

            RegionManager mgr = wg.getGlobalRegionManager().get(world);
            ProtectedRegion region = mgr.getRegion(rid);
            if (region == null) {
                if (rid.equalsIgnoreCase("__global__")) {
                    region = new GlobalProtectedRegion(rid);
                    mgr.addRegion(region);
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + L("command.worldguard.region_not_found"));
                    return true;
                }
            }
            
            CRegion reg = rm.world(world).region(region);
            
            switch (act) {
                case INFO:
                    onInfo(sender, player, reg);
                    return true;
                case FLAG:
                    onFlag(sender, player, reg, args);
                    return true;
            }
            return false;
        }
        
        private void onInfo(CommandSender sender, Player player, CRegion region) {
            String[] args = new String[]{"info", region.getWorld().getName(), region.getProtectedRegion().getId()};
            wg.onCommand(sender, wg.getCommand("region"), "/region", args);
            
            StringBuilder list = new StringBuilder();
            for (FlagValue data : region.getFlags()) {
                if (list.length() > 0)
                    list.append(", ");
                list.append(data.getFlag().getName());
                list.append(": ");
                list.append(data.getValue().toString());
            }
            
            sender.sendMessage(ChatColor.GREEN + L("command.worldguard.additional_flags") + ": " + list.toString());
        }
        private void onFlag(CommandSender sender, Player player, CRegion region, String[] args) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.DARK_RED + L("command.worldguard.no_flag_given"));
                sender.sendMessage(ChatColor.DARK_RED + L("command.worldguard.available_flags") + ": " + FlagList.getStringListAvailableFlags(sender));
                return;
            }
            String flagName = args[2];
            Flag<?> flag = FlagList.getFlag(flagName);
            if (flag == null) {
                sender.sendMessage(ChatColor.DARK_RED + L("command.worldguard.unknown_flag") + ": " + flagName);
                sender.sendMessage(ChatColor.DARK_RED + L("command.worldguard.available_flags") + ": " + FlagList.getStringListAvailableFlags(sender));
                return;
            }
            String value = null;
            if (args.length > 3)
                value = Util.join(args, 3);
            
            try {
                if (value != null) {
                    region.setFlag(flag, flag.parseInput(wg, sender, value));
                } else {
                    region.setFlag(flag, null);
                }
            } catch (InvalidFlagFormat e) {
                sender.sendMessage(ChatColor.DARK_RED + e.getLocalizedMessage());
                return;
            }
            sender.sendMessage(L("command.worldguard.flag_set", flag.getName()));
        }
    }
}
