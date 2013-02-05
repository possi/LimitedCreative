package de.jaschastarke.minecraft.limitedcreative;

import de.jaschastarke.LocaleString;
import de.jaschastarke.bukkit.lib.commands.BukkitCommand;
import de.jaschastarke.bukkit.lib.commands.IHelpDescribed;
import de.jaschastarke.maven.ArchiveDocComments;
import de.jaschastarke.minecraft.lib.permissions.IAbstractPermission;

/**
 * LimitedCreative: GameMode-Switch, Creative-Regions, Config and more
 * @usage /<command> - displays LimitedCreative-Help
 * @permission limitedcreative.command
 */
@ArchiveDocComments
public class MainCommand extends BukkitCommand implements IHelpDescribed {
    private LimitedCreative plugin;
    
    public MainCommand() {
    }
    public MainCommand(LimitedCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "limitedcreative";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"lc"};
    }

    /**
     * @internal has no effect, as not tested by any command handler
     * @see IHelpDescribed
     */
    @Override
    public IAbstractPermission[] getRequiredPermissions() {
        return new IAbstractPermission[]{Permissions.COMMAND};
    }
    @Override
    public String[] getUsages() {
        return null;
    }
    @Override
    public CharSequence getDescription() {
        return new LocaleString("command.general");
    }
    @Override
    public String getPackageName() {
        return plugin.getName();
    }
}
