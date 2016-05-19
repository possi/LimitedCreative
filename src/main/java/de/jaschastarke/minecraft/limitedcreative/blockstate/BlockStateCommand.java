package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

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
import de.jaschastarke.bukkit.lib.database.DBHelper;
import de.jaschastarke.database.DatabaseConfigurationException;
import de.jaschastarke.database.db.Database;
import de.jaschastarke.maven.ArchiveDocComments;
import de.jaschastarke.maven.PluginCommand;
import de.jaschastarke.minecraft.lib.permissions.IAbstractPermission;
import de.jaschastarke.minecraft.limitedcreative.ModBlockStates;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockState.Source;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBModel.Cuboid;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBModel.DBTransaction;
import de.jaschastarke.modularize.ModuleEntry.ModuleState;

/**
 * LimitedCreative-BlockState-Command: modify blockstate database to prevent drops of selected blocks (requires WorldEdit)
 * @usage /<command> - displays Regions-Command-Help
 * @permission limitedcreative.blockstate.command
 */
@ArchiveDocComments
@PluginCommand
public class BlockStateCommand extends BukkitCommand implements IHelpDescribed {
    private ModBlockStates mod;
    private HelpCommand help;
    
    public BlockStateCommand() {
        this.help = this.getDefaultHelpCommand();
    }
    public BlockStateCommand(ModBlockStates mod) {
        super(mod.getPlugin());
        this.help = this.getDefaultHelpCommand();
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
        return new String[]{"..."};
    }

    @Override
    public CharSequence getDescription() {
        return new LocaleString("command.blockstate");
    }

    @Override
    public CharSequence getPackageName() {
        return mod.getPlugin().getName() + " - " + mod.getName();
    }
    
    public boolean execute(final CommandContext context, final String[] args) throws MissingPermissionCommandException, CommandException {
        if (mod.getModuleEntry().getState() != ModuleState.ENABLED)
            throw new CommandException("Module " + mod.getName() + " is disabled");
        return super.execute(context, args);
    }
    
    /**
     * Deletes no longer used data from the BlockState-Database. Currently it only removes non-creative entries
     * from the database, if you changed to "logSurvival"-config from true to false.
     */
    @IsCommand("cleanup")
    @Usages("")
    public boolean cleanupDatabase(final CommandContext context, String... args) throws CommandException {
        if (mod.getConfig().getLogSurvival()) {
            context.responseFormatted(ChatFormattings.INFO, L("command.blockstate.nothing_to_cleanup"));
        } else {
            mod.getPlugin().getServer().getScheduler().runTaskAsynchronously(mod.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    int countDeleted = mod.getModel().cleanUp(DBModel.Cleanup.SURVIVAL);
                    if (countDeleted < 0)
                        context.responseFormatted(ChatFormattings.ERROR, L("command.blockstate.cleanup_error"));
                    else
                        context.responseFormatted(ChatFormattings.SUCCESS, L("command.blockstate.cleanup_success", countDeleted));
                }
            });
        }
        return true;
    }

    /**
     * Modifies the BlockState-Database and sets all blocks in the selection to the provided gamemode. Set it
     * to "creative" to disable drop of this block on destroying. Set it to "survival" to allow it.
     * WorldEdit is required, because the selection Region is used.
     * gamemode can be: survival / creative / adventure / s / c / a / 0 / 1 / 2
     * @throws MissingPermissionCommandException 
     */
    @IsCommand("set")
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
        if (mod.getConfig().getIgnoredWorlds().contains(context.getPlayer().getWorld().getName())) {
            context.response(L("command.blockstate.world_ignored", context.getPlayer().getWorld().getName()));
            return true;
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
        
        mod.getPlugin().getServer().getScheduler().runTaskAsynchronously(mod.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (mod.isDebug())
                    mod.getLog().debug("Scheduler: Asynchronous Task run");
                DBTransaction update = mod.getModel().groupUpdate();
                int count = 0;
                World w = selection.getWorld();
                
                Cuboid c = new Cuboid();
                c.add(min);
                c.add(max);
                mod.getModel().cacheStates(c);
                
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
                                update.setState(new BlockState(seed));
                                count++;
                            }
                        }
                    }
                }
                update.finish();
                
                context.response(L("command.blockstate.command_updated", count));
            }
        });
        return true;
    }

    /**
     * Imports BlockState Data from a given Database to the current active Database.
     * A Server-Restart is needed after migration!
     * Parameters:
     *  -u  --update            Don't delete existing records / only overwrite if newer
     *      --import=<type>     Import from other Plugins. Supported Types:
     *               cc             CreativeControl
     */
    @IsCommand("migrate")
    @Usages("-u --import=cc <dsn> [username] [password]")
    public boolean migrateDatabase(final CommandContext context, String... args) throws CommandException, MissingPermissionCommandException {
        DefinedParameterParser params = new DefinedParameterParser(args, new String[]{"debug", "d", "update", "u", "confirm"});
        if (params.getArgumentCount() < 1) {// doesn't count parameters
            return false;
        }
        
        if (Bukkit.getServer().getOnlinePlayers().size() > (context.isPlayer() ? 1 : 0)) {
            context.responseFormatted(ChatFormattings.ERROR, L("command.blockstate.migrate_useronline_error"));
            return true;
        }
        
        Database source;
        Database target;
        try {
            
            if (params.getArgumentCount() < 2)
                source = DBHelper.createConnection(params.getArgument(0));
            else if (params.getArgumentCount() < 3)
                source = DBHelper.createConnection(params.getArgument(0), params.getArgument(1), null);
            else
                source = DBHelper.createConnection(params.getArgument(0), params.getArgument(1), params.getArgument(2));
            
            target = mod.getPlugin().getDatabaseConnection();
        } catch (DatabaseConfigurationException e) {
            context.responseFormatted(ChatFormattings.ERROR, L("command.blockstate.migrate_connect_error", e.getMessage()));
            return true;
        }
        
        DatabaseMigrationThread thread;
        if (params.getParameter("import") != null) {
            if (params.getParameter("import").equals("cc")) {
                thread = new CreativeControlImportThread(mod, context, source, target);
            } else {
                context.responseFormatted(ChatFormattings.ERROR, L("command.blockstate.migrate_importtype_error", params.getParameter("import")));
                return false;
            }
        } else {
             thread = new DatabaseMigrationThread(mod, context, source, target);
        }
        if (params.getFlags().contains("update") || params.getFlags().contains("u")) {
            thread.setMode(DatabaseMigrationThread.Mode.UPDATE);
        }
        if (params.getFlags().contains("debug") || params.getFlags().contains("d")) {
            thread.setDebug(true);
        }
        
        if (!params.getFlags().contains("confirm")) {
            context.responseFormatted(ChatFormattings.INFO, L("command.blockstate.migrate_confirm", "--confirm"));
            return true;
        }
        
        mod.getModuleEntry().disable();
        
        thread.start();
        String sourceType = source.getType().toString();
        if (params.getParameter("import") != null) {
            if (params.getParameter("import").equals("cc")) {
                sourceType = "CreativeControl-" + sourceType;
            }
        }
        context.response(L("command.blockstate.migrate_started", sourceType, target.getType()));
        return true;
    }
    
    private String L(String msg, Object... args) {
        return mod.getPlugin().getLocale().trans(msg, args);
    }
}
