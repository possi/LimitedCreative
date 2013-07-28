package de.jaschastarke.minecraft.limitedcreative.regions;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.World;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.jaschastarke.LocaleString;
import de.jaschastarke.bukkit.lib.chat.ChatFormattings;
import de.jaschastarke.bukkit.lib.commands.BukkitCommand;
import de.jaschastarke.bukkit.lib.commands.CommandContext;
import de.jaschastarke.bukkit.lib.commands.CommandException;
import de.jaschastarke.bukkit.lib.commands.HelpCommand;
import de.jaschastarke.bukkit.lib.commands.IHelpDescribed;
import de.jaschastarke.bukkit.lib.commands.MissingPermissionCommandException;
import de.jaschastarke.bukkit.lib.commands.annotations.IsCommand;
import de.jaschastarke.bukkit.lib.commands.annotations.Usages;
import de.jaschastarke.bukkit.lib.commands.parser.DefinedParameterParser;
import de.jaschastarke.maven.ArchiveDocComments;
import de.jaschastarke.minecraft.lib.permissions.IAbstractPermission;
import de.jaschastarke.minecraft.limitedcreative.ModRegions;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.FlagList;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.FlagValue;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.Region;

/**
 * LimitedCreative-Region-Command: configure creative regions
 * @usage /<command> - displays Regions-Command-Help
 * @permission limitedcreative.region
 */
@ArchiveDocComments
public class RegionsCommand extends BukkitCommand implements IHelpDescribed {
    private ModRegions mod;
    private HelpCommand help;
    private WorldGuardPlugin wg;
    
    public RegionsCommand() {
        this.help = this.getDefaultHelpCommand();
    }
    public RegionsCommand(ModRegions mod) {
        this();
        this.mod = mod;
        this.wg = (WorldGuardPlugin) mod.getPlugin().getServer().getPluginManager().getPlugin("WorldGaurd");
    }
    
    @Override
    public String getName() {
        return "lcr";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"/region"};
    }

    /**
     * @internal has no effect, as not tested by any command handler
     * @see IHelpDescribed
     */
    @Override
    public IAbstractPermission[] getRequiredPermissions() {
        return new IAbstractPermission[]{RegionPermissions.REGION};
    }
    @Override
    public String[] getUsages() {
        return null;
    }
    @Override
    public CharSequence getDescription() {
        return new LocaleString("command.regions");
    }
    @Override
    public String getPackageName() {
        return mod.getPlugin().getName() + " - " + mod.getName();
    }
    
    /*@Override
    public IPermission getPermission(String subPerm) {
        if (subPerm.equals("region"))
            return RegionPermissions.REGION;
        else
            return RegionPermissions.REGION.getPermission(subPerm);
    }*/
    
    /**
     * Sets the Flag of a region to a new value. If no value given, the flag is removed.
     * -g sets the affected group of the flag, instead the flag (equivalent to using flag-group as flag-name)
     * -w world uses a world by name instead the world your in (required from console)
     */
    @IsCommand("flag")
    //@NeedsPermission("region") // not needed as the whole command requires permissions
    @Usages("<region> -g <flag> -w world [value]")
    public boolean setFlag(CommandContext context, String... args) throws CommandException, MissingPermissionCommandException {
        DefinedParameterParser params = new DefinedParameterParser(args, new String[]{"g"}, 2);
        if (params.getArgumentCount() < 2) {// doesn't count parameters
            help.execute(context, new String[]{"flag"});
            context.response(L("command.worldguard.available_flags") + FlagList.getStringListAvailableFlags(context.getSender()));
            return true;
        }
        
        World w = context.isPlayer() ? context.getPlayer().getWorld() : null;
        if (params.getParameter("-w") != null)
            w = mod.getPlugin().getServer().getWorld(params.getParameter("-w"));
        if (w == null)
            throw new CommandException(L("command.worldguard.world_not_found"));
        
        RegionManager mgr = getWorldGuard().getGlobalRegionManager().get(w);
        ProtectedRegion region = mgr.getRegion(params.getArgument(0));
        if (region == null && params.getArgument(0).equalsIgnoreCase("__global__")) {
            region = new GlobalProtectedRegion(params.getArgument(0));
            mgr.addRegion(region);
        }
        if (region == null)
            throw new CommandException(L("command.worldguard.region_not_found"));

        Region reg = mod.getRegionManager().world(w).region(region);
        
        Flag<?> flag = FlagList.getFlag(params.getArgument(1));
        if (flag == null) {
            String msg = L("command.worldguard.unknown_flag") + params.getArgument(1) + "\n"
                       + L("command.worldguard.available_flags") + FlagList.getStringListAvailableFlags(context.getSender());
            throw new CommandException(msg);
        }
        
        String value = params.getValue();
        try {
            if (value != null) {
                reg.setFlag(flag, flag.parseInput(getWorldGuard(), context.getSender(), value));
            } else {
                reg.setFlag(flag, null);
            }
        } catch (InvalidFlagFormat e) {
            context.response(context.getFormatter().formatString(ChatFormattings.ERROR, e.getLocalizedMessage()));
            return true;
        }
        
        context.response(L("command.worldguard.flag_set", flag.getName()));
        return true;
    }
    
    @IsCommand("info")
    //@NeedsPermission("region")
    @Usages("[world] [id]")
    public boolean getInfo(CommandContext context, String... args) throws CommandException {
        DefinedParameterParser params = new DefinedParameterParser(args, new String[]{"s"}, 1);
        
        if (context.isPlayer()) {
            /*
             * WorldEdits intercepting Servers privates commandMap via Reflections realy sucks!
             * Just because they are to lazy to add all the lines commands to plugin.yml
             */
            String orgCmd = ("region info " + StringUtils.join(args)).trim();
            mod.getPlugin().getServer().dispatchCommand(context.getSender(), orgCmd);
        }
        
        World w = context.isPlayer() ? context.getPlayer().getWorld() : null;
        if (params.getArgumentCount() > 1)
            w = mod.getPlugin().getServer().getWorld(params.getArgument(0));
        if (w == null)
            throw new CommandException(L("command.worldguard.world_not_found"));
        
        ProtectedRegion region = null;
        if (params.getArgumentCount() == 0 && context.isPlayer()) {
            RegionManager mgr = getWorldGuard().getGlobalRegionManager().get(context.getPlayer().getWorld());
            ApplicableRegionSet set = mgr.getApplicableRegions(context.getPlayer().getLocation());
            if (set.size() > 0) {
                region = set.iterator().next();
            }
        } else {
            int rpc = params.getArgumentCount() > 1 ? 1 : 0;
            RegionManager mgr = getWorldGuard().getGlobalRegionManager().get(w);
            region = mgr.getRegion(params.getArgument(rpc));
            if (region == null && params.getArgument(rpc).equalsIgnoreCase("__global__")) {
                region = new GlobalProtectedRegion(params.getArgument(rpc));
                mgr.addRegion(region);
            }
        }
        if (region == null)
            throw new CommandException(L("command.worldguard.region_not_found"));

        Region reg = mod.getRegionManager().world(w).region(region);
        
        StringBuilder list = new StringBuilder();
        for (FlagValue data : reg.getFlags()) {
            if (list.length() > 0)
                list.append(", ");
            list.append(data.getFlag().getName());
            list.append(": ");
            list.append(data.getValue().toString());
        }
        
        context.response(ChatColor.GREEN + L("command.worldguard.additional_flags") + list.toString());
        return true;
    }
    
    private String L(String msg, Object... args) {
        return mod.getPlugin().getLocale().trans(msg, args);
    }
    
    private WorldGuardPlugin getWorldGuard() {
        return wg;
    }
}
