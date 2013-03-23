package de.jaschastarke.minecraft.limitedcreative;

import java.io.IOException;

import org.bukkit.event.Listener;

import de.jaschastarke.bukkit.lib.CoreModule;
import de.jaschastarke.bukkit.tools.stats.IStatistics;
import de.jaschastarke.bukkit.tools.stats.PiwikStatistics;

public class FeatureMetrics extends CoreModule<LimitedCreative> implements Listener {
    public FeatureMetrics(LimitedCreative plugin) {
        super(plugin);
    }
    private IStatistics metric;
    
    @Override
    public void onEnable() {
        super.onEnable();
        metric = new PiwikStatistics(plugin);
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
        metric.unregister();
    }

    public void track(String event) throws IOException {
        if (metric == null)
            throw new IllegalAccessError("The feature hasn't been enabled");
        metric.trackEvent(event);
    }
}
