package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.sql.SQLException;
import java.util.Date;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

import de.jaschastarke.LocaleString;
import de.jaschastarke.bukkit.lib.commands.BukkitCommand;
import de.jaschastarke.bukkit.lib.commands.CommandContext;
import de.jaschastarke.bukkit.lib.commands.CommandException;
import de.jaschastarke.bukkit.lib.commands.HelpCommand;
import de.jaschastarke.bukkit.lib.commands.IHelpDescribed;
import de.jaschastarke.bukkit.lib.commands.MissingPermissionCommandException;
import de.jaschastarke.bukkit.lib.commands.annotations.IsCommand;
import de.jaschastarke.bukkit.lib.commands.annotations.Usages;
import de.jaschastarke.maven.ArchiveDocComments;
import de.jaschastarke.minecraft.lib.permissions.IAbstractPermission;
import de.jaschastarke.minecraft.limitedcreative.ModBlockStates;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockState.Source;

/**
 * LimitedCreative-BlockState-Command: modify blockstate database to prevent drops of selected blocks (requires WorldEdit)
 * @usage /<command> - displays Regions-Command-Help
 * @permission limitedcreative.blockstate.command
 */
@ArchiveDocComments
public class BlockStateCommand extends BukkitCommand implements IHelpDescribed {
    private ModBlockStates mod;
    private HelpCommand help;
    
    public BlockStateCommand() {
        this.help = this.getDefaultHelpCommand();
    }
    public BlockStateCommand(ModBlockStates mod) {
        this();
        this.mod = mod;
    }
    
    @Override
    public String getName() {
        return "lcbs";
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    /**
     * @internal has no effect, as not tested by any command handler
     * @see IHelpDescribed
     */
    @Override
    public IAbstractPermission[] getRequiredPermissions() {
        return new IAbstractPermission[]{BlockStatePermissions.COMMAND};
    }

    @Override
    public CharSequence[] getUsages() {
        return null;
    }

    @Override
    public CharSequence getDescription() {
        return new LocaleString("command.blockstate");
    }

    @Override
    public CharSequence getPackageName() {
        return mod.getPlugin().getName() + " - " + mod.getName();
    }

    /**
     * Modifies the Block-GameMode-Database and sets all blocks in the selection to the provided gamemode. Set it
     * to "creative" to disable drop of this block on destroying. Set it to "survival" to allow it.
     * WorldEdit is required, because the selection Region is used.
     * gamemode can be: survival / creative / adventure / s / c / a / 0 / 1 / 2
     * @throws MissingPermissionCommandException 
     */
    @IsCommand("set")
    //@NeedsPermission("region")
    @Usages("<gamemode>")
    public boolean setGameMode(final CommandContext context, String... args) throws CommandException, MissingPermissionCommandException {
        if (!mod.getPlugin().getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
            help.execute(context, new String[]{"set"});
            context.response(L("command.blockstate.requires_worldedit"));
            return true;
        }
        if (!context.isPlayer()) {
            context.response(L("cmdblock.blocked.not_console"));
            return true;
        }
        if (args.length < 1) {// doesn't count parameters
            return false;
        }
        String gm = args[0].toLowerCase();
        final GameMode tgm;
        if (gm.equals("0") || gm.equals("s") || gm.equals("survival"))
            tgm = GameMode.SURVIVAL;
        else if (gm.equals("1") || gm.equals("c") || gm.equals("creative"))
            tgm = GameMode.CREATIVE;
        else if (gm.equals("2") || gm.equals("a") || gm.equals("adventure"))
            tgm = GameMode.ADVENTURE;
        else {
            return false;
        }
        
        WorldEditPlugin we = (WorldEditPlugin) mod.getPlugin().getServer().getPluginManager().getPlugin("WorldEdit");
        final Selection selection = we.getSelection(context.getPlayer());
        
        if (selection == null) {
            context.response(L("command.blockstate.worledit_selection_empty"));
            return true;
        }

        final Location min = selection.getMinimumPoint();
        final Location max = selection.getMaximumPoint();
        
        mod.getPlugin().getServer().getScheduler().runTaskAsynchronously(mod.getPlugin(), new BukkitRunnable() {
            @Override
            public void run() {
                DBQueries q = mod.getQueries();
                try {
                    q.getDB().startTransaction();
                    int count = 0;
                    World w = selection.getWorld();
                    BlockState seed = new BlockState();
                    seed.setPlayer(context.getPlayer());
                    seed.setGameMode(tgm);
                    seed.setSource(Source.COMMAND);
                    seed.setDate(new Date());
                    for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                        for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                            for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                                Location loc = new Location(w, x, y, z);
                                if (w.getBlockAt(loc).getType() != Material.AIR && selection.contains(loc)) {
                                    seed.setLocation(loc);
                                    q.delete(loc);
                                    q.insert(seed);
                                    //q.replace(seed);
                                    count++;
                                }
                            }
                        }
                    }
                    q.getDB().endTransaction();
                    
                    context.response(L("command.blockstate.command_updated", count));
                } catch (SQLException e) {
                    try {
                        q.getDB().revertTransaction();
                    } catch (SQLException e1) {
                    }
                    mod.getLog().warn("Failed to update blocks in region: " + e.getMessage());
                    context.response(L("command.blockstate.command_failed"));
                }
            }
        });
        return true;
    }
    
    private String L(String msg, Object... args) {
        return mod.getPlugin().getLocale().trans(msg, args);
    }
}
