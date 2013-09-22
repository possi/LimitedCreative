package de.jaschastarke.minecraft.limitedcreative;

import de.jaschastarke.bukkit.lib.CoreModule;
import de.jaschastarke.bukkit.lib.commands.AliasHelpedCommand;
import de.jaschastarke.bukkit.lib.modules.AdditionalBlockBreaks;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockListener;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockStateCommand;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockStateConfig;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBModel;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBQueries;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DependencyListener;
import de.jaschastarke.minecraft.limitedcreative.blockstate.HangingListener;
import de.jaschastarke.minecraft.limitedcreative.blockstate.PlayerListener;
import de.jaschastarke.minecraft.limitedcreative.blockstate.worldedit.LCEditSessionFactory;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;
import de.jaschastarke.modularize.ModuleEntry.ModuleState;

public class ModBlockStates extends CoreModule<LimitedCreative> {
    private BlockStateConfig config;
    private FeatureBlockItemSpawn blockDrops;
    private DBQueries queries;
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
        
        blockDrops = plugin.getModule(FeatureBlockItemSpawn.class);
        if (blockDrops == null)
            blockDrops = plugin.addModule(new FeatureBlockItemSpawn(plugin)).getModule();
        
        config = new BlockStateConfig(this, entry);
        plugin.getPluginConfig().registerSection(config);
        
        if (plugin.getModule(AdditionalBlockBreaks.class) == null) {
            plugin.addModule(new AdditionalBlockBreaks(plugin));
        }
        
        listeners.addListener(new BlockListener(this));
        listeners.addListener(new HangingListener(this));
        listeners.addListener(new PlayerListener(this));
        listeners.addListener(new DependencyListener(this));
        
        command = new BlockStateCommand(this);
    }
    @Override
    public void onEnable() {
        try {
            queries = new DBQueries(this, getPlugin().getDatabaseConnection());
            queries.initTable();
        } catch (Exception e) {
            e.printStackTrace();
            getLog().warn(plugin.getLocale().trans("block_state.error.sql_connection_failed", getName()));
            entry.initialState = ModuleState.NOT_INITIALIZED;
            return;
        }
        super.onEnable();
        try {
            if (plugin.getServer().getPluginManager().isPluginEnabled("WorldEdit"))
                LCEditSessionFactory.initFactory(this);
        } catch (Exception e) {
            getLog().warn(plugin.getLocale().trans("block_state.warning.worldedit_sessionfactory_failed", e.getMessage()));
        }
        
        plugin.getCommandHandler().registerCommand(command);
        plugin.getMainCommand().registerCommand(new AliasHelpedCommand<BlockStateCommand>(command, "blockstate", new String[]{"bs"}));
        
        getLog().info(plugin.getLocale().trans("basic.loaded.module"));
    }
    @Override
    public void onDisable() {
        super.onDisable();
    }
    
    public BlockStateConfig getConfig() {
        return config;
    }
    public FeatureBlockItemSpawn getBlockSpawn() {
        return blockDrops;
    }
    public DBQueries getQueries() {
        return queries;
    }
    public DBModel getModel() {
        if (model == null)
            model = new DBModel(this);
        return model;
    }
}
