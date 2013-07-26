package de.jaschastarke.minecraft.limitedcreative;

import de.jaschastarke.bukkit.lib.CoreModule;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockListener;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockStateConfig;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBQueries;
import de.jaschastarke.minecraft.limitedcreative.blockstate.PlayerListener;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;
import de.jaschastarke.modularize.ModuleEntry.ModuleState;

public class ModBlockStates extends CoreModule<LimitedCreative> {
    private BlockStateConfig config;
    private FeatureBlockItemSpawn blockDrops;
    private DBQueries queries;

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
        
        listeners.addListener(new BlockListener(this));
        listeners.addListener(new PlayerListener(this));
    }
    @Override
    public void onEnable() {
        try {
            queries = new DBQueries(getPlugin().getDatabaseConnection());
            queries.initTable();
        } catch (Exception e) {
            e.printStackTrace();
            getLog().warn(plugin.getLocale().trans("block_state.error.sql_connection_failed", getName()));
            entry.initialState = ModuleState.NOT_INITIALIZED;
            return;
        }
        super.onEnable();
        
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
}
