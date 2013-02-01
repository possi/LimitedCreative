package de.jaschastarke.minecraft.limitedcreative;

import org.bukkit.event.Listener;

import de.jaschastarke.bukkit.lib.CoreModule;
import de.jaschastarke.bukkit.tools.stats.IStatistics;
import de.jaschastarke.bukkit.tools.stats.PiwikStatistics;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;
import de.jaschastarke.modularize.ModuleEntry.ModuleState;

public class FeatureMetrics extends CoreModule<LimitedCreative> implements Listener {
    public FeatureMetrics(LimitedCreative plugin) {
        super(plugin);
    }
    private IStatistics metric;
    
    @Override
    public void initialize(ModuleEntry<IModule> pEntry) {
        super.initialize(pEntry);
        if (!plugin.getPluginConfig().getMetrics()) {
            pEntry.initialState = ModuleState.DISABLED;
        }
    }

    @Override
    public void onEnable() {
        metric = new PiwikStatistics(plugin);
    }
    
    @Override
    public void onDisable() {
        metric.unregister();
    }

    public void track(String event) {
        if (metric == null)
            throw new IllegalAccessError("The feature hasn't been enabled");
        metric.trackEvent(event);
    }
}
