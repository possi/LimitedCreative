package de.jaschastarke.minecraft.limitedcreative;

import java.util.WeakHashMap;

import org.bukkit.entity.Entity;

import de.jaschastarke.bukkit.lib.CoreModule;
import de.jaschastarke.bukkit.lib.modules.AdditionalBlockBreaks;
import de.jaschastarke.minecraft.limitedcreative.limits.BlockListener;
import de.jaschastarke.minecraft.limitedcreative.limits.EntityListener;
import de.jaschastarke.minecraft.limitedcreative.limits.LimitConfig;
import de.jaschastarke.minecraft.limitedcreative.limits.PlayerListener;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;

public class ModCreativeLimits extends CoreModule<LimitedCreative> {
    protected LimitConfig config;
    private WeakHashMap<Entity, Void> no_xp_mobs = new WeakHashMap<Entity, Void>();
    
    public ModCreativeLimits(LimitedCreative plugin) {
        super(plugin);
    }
    
    protected FeatureBlockItemSpawn blockDrops = null;
    
    @Override
    public String getName() {
        return "Limits";
    }

    @Override
    public void initialize(ModuleEntry<IModule> entry) {
        super.initialize(entry);
        listeners.addListener(new PlayerListener(this));
        listeners.addListener(new EntityListener(this));
        listeners.addListener(new BlockListener(this));
        config = plugin.getPluginConfig().registerSection(new LimitConfig(this, entry));
        
        blockDrops = modules.linkSharedModule(FeatureBlockItemSpawn.class, plugin.getModules());
        modules.linkSharedModule(AdditionalBlockBreaks.class, plugin.getModules());
    }
    
    public FeatureBlockItemSpawn getBlockSpawn() {
        return blockDrops;
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        getLog().info(plugin.getLocale().trans("basic.loaded.module"));
    }

    public LimitConfig getConfig() {
        return config;
    }
    
    public WeakHashMap<Entity, Void> getNoXPMobs() {
        return no_xp_mobs;
    }

}
