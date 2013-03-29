package de.jaschastarke.minecraft.limitedcreative;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;

import de.jaschastarke.bukkit.lib.CoreModule;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockListener;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockState;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockStateConfig;
import de.jaschastarke.minecraft.limitedcreative.blockstate.PlayerListener;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;

public class ModBlockStates extends CoreModule<LimitedCreative> {
    private BlockStateConfig config;
    private FeatureBlockItemSpawn blockDrops;

    public ModBlockStates(LimitedCreative plugin) {
        super(plugin);
    }
    @Override
    public String getName() {
        return "GameModePerm";
    }

    @Override
    public void initialize(ModuleEntry<IModule> entry) {
        super.initialize(entry);
        
        blockDrops = plugin.getModule(FeatureBlockItemSpawn.class);
        if (blockDrops == null)
            blockDrops = plugin.addModule(new FeatureBlockItemSpawn(plugin)).getModule();
        
        listeners.addListener(new BlockListener(this));
        listeners.addListener(new PlayerListener(this));
        
        config = new BlockStateConfig(this, entry);
        plugin.getPluginConfig().registerSection(config);
        plugin.getDatabaseManager().registerDatabaseClass(BlockState.class);
    }
    @Override
    public void onEnable() {
        super.onEnable();
        
        getLog().info(plugin.getLocale().trans("basic.loaded.module"));
    }
    @Override
    public void onDisable() {
        super.onDisable();;
    }
    
    public EbeanServer getDB() {
        return plugin.getDatabaseManager().getDatabase();
    }
    public Query<BlockState> getBSQuery() {
        return plugin.getDatabaseManager().getDatabase().find(BlockState.class);
    }
    
    public BlockStateConfig getConfig() {
        return config;
    }
    public FeatureBlockItemSpawn getBlockSpawn() {
        return blockDrops;
    }
}
