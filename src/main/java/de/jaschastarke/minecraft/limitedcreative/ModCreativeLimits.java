package de.jaschastarke.minecraft.limitedcreative;

import de.jaschastarke.bukkit.lib.CoreModule;
import de.jaschastarke.minecraft.limitedcreative.limits.LimitConfig;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;

public class ModCreativeLimits extends CoreModule<LimitedCreative> {
    protected LimitConfig config;
    
    public ModCreativeLimits(LimitedCreative plugin) {
        super(plugin);
    }
    
    public LimitConfig getConfig() {
        return config;
    }
    
    protected FeatureBlockItemSpawn blockDrops = null;

    @Override
    public void initialize(ModuleEntry<IModule> entry) {
        super.initialize(entry);
        blockDrops = plugin.getModule(FeatureBlockItemSpawn.class);
        if (blockDrops == null)
            blockDrops = plugin.addModule(new FeatureBlockItemSpawn(plugin)).getModule();
        
    }

}
