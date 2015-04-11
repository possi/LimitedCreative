package de.jaschastarke.minecraft.limitedcreative;


import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.WorldEdit;

import de.jaschastarke.bukkit.lib.CoreModule;
import de.jaschastarke.bukkit.lib.commands.AliasHelpedCommand;
import de.jaschastarke.bukkit.lib.modules.AdditionalBlockBreaks;
import de.jaschastarke.bukkit.lib.modules.BlockFall;
import de.jaschastarke.bukkit.lib.modules.InstantHangingBreak;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockListener;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockStateCommand;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockStateConfig;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBModel;
import de.jaschastarke.minecraft.limitedcreative.blockstate.HangingStandingListener;
import de.jaschastarke.minecraft.limitedcreative.blockstate.PlayerListener;
import de.jaschastarke.minecraft.limitedcreative.blockstate.SyncronizedModel;
import de.jaschastarke.minecraft.limitedcreative.blockstate.ThreadedModel;
import de.jaschastarke.minecraft.limitedcreative.blockstate.worldedit.EditSessionListener;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;

public class ModBlockStates extends CoreModule<LimitedCreative> {
    private BlockStateConfig config;
    private FeatureBlockItemSpawn blockDrops;
    private BlockStateCommand command;
    private DBModel model;

    public ModBlockStates(LimitedCreative plugin) {
        super(plugin);
    }
    @Override
    public String getName() {
        return "BlockState";
    }

    @Override
    public void initialize(ModuleEntry<IModule> entry) {
        super.initialize(entry);
        
        config = new BlockStateConfig(this, entry);
        plugin.getPluginConfig().registerSection(config);
        
        blockDrops = modules.linkSharedModule(FeatureBlockItemSpawn.class, plugin.getModules());
        modules.linkSharedModule(AdditionalBlockBreaks.class, plugin.getModules());
        modules.linkSharedModule(InstantHangingBreak.class, plugin.getModules());
        this.addModule(new BlockFall(plugin));
        
        listeners.addListener(new BlockListener(this));
        listeners.addListener(new HangingStandingListener(this));
        listeners.addListener(new PlayerListener(this));
        
        command = new BlockStateCommand(this);
    }
    @Override
    public void onEnable() {
        try {
            if (model == null) {
                if (config.getUseThreading())
                    model = new ThreadedModel(this);
                else
                    model = new SyncronizedModel(this);
            }
            if (model instanceof Listener)
                listeners.addListener((Listener) model);
            model.onEnable();
        } catch (Exception e) {
            e.printStackTrace();
            getLog().warn(plugin.getLocale().trans("block_state.error.sql_connection_failed", getName()));
            entry.deactivateUsage();
            return;
        }
        super.onEnable();
        if (plugin.getServer().getPluginManager().isPluginEnabled("WorldEdit") && config.getWorldeditIntegration()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        WorldEdit.getInstance().getEventBus().register(new EditSessionListener(ModBlockStates.this));
                    } catch (Exception e) {
                        getLog().warn(plugin.getLocale().trans("block_state.warning.worldedit_sessionfactory_failed", e.getMessage()));
                    }
                }
            }.runTaskLater(plugin, 1L);
        }
        
        plugin.getCommandHandler().registerCommand(command);
        plugin.getMainCommand().registerCommand(new AliasHelpedCommand<BlockStateCommand>(command, "blockstate", new String[]{"bs"}));
        
        getLog().info(plugin.getLocale().trans("basic.loaded.module"));
    }
    @Override
    public void onDisable() {
        if (model != null)
            model.onDisable();
        super.onDisable();
        if (model != null && model instanceof Listener)
            listeners.removeListener((Listener) model);
        model = null;
        
        plugin.getCommandHandler().removeCommand(command);
        plugin.getMainCommand().removeCommand(command);
    }
    
    public BlockStateConfig getConfig() {
        return config;
    }
    public FeatureBlockItemSpawn getBlockSpawn() {
        return blockDrops;
    }
    public DBModel getModel() {
        return model;
    }
}
