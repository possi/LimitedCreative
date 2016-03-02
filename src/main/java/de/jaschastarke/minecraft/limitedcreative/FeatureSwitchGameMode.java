package de.jaschastarke.minecraft.limitedcreative;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;

import de.jaschastarke.IHasName;
import de.jaschastarke.bukkit.lib.CoreModule;
import de.jaschastarke.bukkit.lib.commands.AbstractCommandList;
import de.jaschastarke.bukkit.lib.commands.CommandContext;
import de.jaschastarke.bukkit.lib.commands.CommandException;
import de.jaschastarke.bukkit.lib.commands.ICommand;
import de.jaschastarke.bukkit.lib.commands.MethodCommand;
import de.jaschastarke.bukkit.lib.commands.IMethodCommandContainer;
import de.jaschastarke.bukkit.lib.commands.MissingPermissionCommandException;
import de.jaschastarke.bukkit.lib.commands.NeedsPlayerArgumentCommandException;
import de.jaschastarke.bukkit.lib.commands.annotations.Alias;
import de.jaschastarke.bukkit.lib.commands.annotations.Description;
import de.jaschastarke.bukkit.lib.commands.annotations.IsCommand;
import de.jaschastarke.bukkit.lib.commands.annotations.NeedsPermission;
import de.jaschastarke.bukkit.lib.commands.annotations.Usages;
import de.jaschastarke.bukkit.lib.commands.parser.TabCompletion;
import de.jaschastarke.minecraft.lib.permissions.IAbstractPermission;
import de.jaschastarke.minecraft.lib.permissions.IPermission;
import de.jaschastarke.modularize.ModuleEntry.ModuleState;

public class FeatureSwitchGameMode extends CoreModule<LimitedCreative> {
    public FeatureSwitchGameMode(LimitedCreative plugin) {
        super(plugin);
    }
    protected Commands commands = null;
    
    @Override
    public String getName() {
        return "SwitchGameMode";
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (commands == null)
            commands = new Commands();
        plugin.getMainCommand().getHandler().registerCommands(commands.getCommandList());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (commands != null)
            plugin.getMainCommand().getHandler().removeCommands(commands.getCommandList());
    }

    public class Commands extends AbstractCommandList implements IMethodCommandContainer, IHasName {
        private MethodCommand[] commands = MethodCommand.getMethodCommandsFor(this);
        public List<ICommand> getCommandList() {
            return Arrays.asList((ICommand[]) commands);
        }
        
        @Override
        public String getName() {
            return plugin.getName() + " - " + plugin.getLang().trans("basic.feature.gamemodecommands");
        }
        @Override
        public IPermission getPermission(String subPerm) {
            return SwitchGameModePermissions.ALL.getPermission(subPerm);
        }
        
        @SuppressWarnings("deprecation")
        protected boolean changeGameMode(CommandContext context, String player, GameMode tgm, IAbstractPermission permission) throws MissingPermissionCommandException, CommandException {
            Player target = null;
            if (player != null && !player.isEmpty()) {
                target = Bukkit.getPlayer(player);
                if (target == null)
                    throw new CommandException("Player " + player + " not found");
            } else if (context.isPlayer()) {
                target = context.getPlayer();
            }
            
            if (target == null)
                throw new NeedsPlayerArgumentCommandException();
            
            if (!target.equals(context.getSender()) && !context.checkPermission(SwitchGameModePermissions.OTHER))
                throw new MissingPermissionCommandException(SwitchGameModePermissions.OTHER);
            
            GameMode wgm = this.getDefaultGameMode(target.getWorld());
            
            if (!this.regionOptional(target, tgm)) {
                if (!context.checkPermission(permission) && (wgm != tgm || !context.checkPermission(SwitchGameModePermissions.BACKONLY)))
                    throw new MissingPermissionCommandException(permission);
            }
            
            if (target.getGameMode() != tgm) {
                target.setGameMode(tgm);
                if (!context.isPlayer() || !target.equals(context.getPlayer())) {
                    context.response(context.getFormatter().getString("command.gamemode.changed", target.getName()));
                }
            } else {
                context.response(context.getFormatter().getString("command.gamemode.no_change"));
            }
            return true;
        }
        
        private GameMode getDefaultGameMode(World world) {
            return Hooks.DefaultWorldGameMode.get(world);
        }

        private boolean regionOptional(Player player, GameMode tgm) {
            ModRegions mod = plugin.getModule(ModRegions.class);
            return mod != null && mod.getModuleEntry().getState() == ModuleState.ENABLED
                    && mod.getWorldGuardIntegration().isRegionOptional(player, tgm);
        }

        @IsCommand("survival")
        @Alias("s")
        @Description(value = "command.switch.survival", translate = true)
        @NeedsPermission(value={"survival", "backonly"}, optional = true)
        @Usages("[player]")
        public boolean survival(CommandContext context, String player) throws MissingPermissionCommandException, CommandException {
            return changeGameMode(context, player, GameMode.SURVIVAL, SwitchGameModePermissions.SURVIVAL);
        }
        @IsCommand("creative")
        @Alias("c")
        @Description(value = "command.switch.creative", translate = true)
        @NeedsPermission(value={"creative", "backonly"}, optional = true)
        @Usages("[player]")
        public boolean creative(CommandContext context, String player) throws MissingPermissionCommandException, CommandException {
            return changeGameMode(context, player, GameMode.CREATIVE, SwitchGameModePermissions.CREATIVE);
        }
        @IsCommand("adventure")
        @Alias("a")
        @Description(value = "command.switch.adventure", translate = true)
        @NeedsPermission(value={"adventure", "backonly"}, optional = true)
        @Usages("[player]")
        public boolean adventure(CommandContext context, String player) throws MissingPermissionCommandException, CommandException {
            return changeGameMode(context, player, GameMode.ADVENTURE, SwitchGameModePermissions.ADVENTURE);
        }
        @IsCommand("spectator")
        @Alias("sp")
        @Description(value = "command.switch.spectator", translate = true)
        @NeedsPermission(value={"spectator", "backonly"}, optional = true)
        @Usages("[player]")
        public boolean spectator(CommandContext context, String player) throws MissingPermissionCommandException, CommandException {
            return changeGameMode(context, player, GameMode.SPECTATOR, SwitchGameModePermissions.SPECTATOR);
        }
        
        @Override
        public List<TabCompletion> getTabCompleter(MethodCommand cmd) {
            return null;
        }
    }
}
