package de.jaschastarke.minecraft.limitedcreative;

import de.jaschastarke.bukkit.lib.CoreModule;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;

public class ModCreativeLimits extends CoreModule<LimitedCreative> {
    public ModCreativeLimits(LimitedCreative plugin) {
        super(plugin);
    }
    
    protected FeatureBlockItemSpawn blockDrops = null;

    @Override
    public void Initialize(ModuleEntry<IModule> entry) {
        super.Initialize(entry);
        blockDrops = plugin.getModule(FeatureBlockItemSpawn.class);
        if (blockDrops == null)
            blockDrops = plugin.addModule(new FeatureBlockItemSpawn(plugin)).getModule();
        
    }

}
