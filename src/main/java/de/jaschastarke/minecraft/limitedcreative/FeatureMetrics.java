package de.jaschastarke.minecraft.limitedcreative;

import java.io.IOException;

import org.bukkit.event.Listener;

import de.jaschastarke.bukkit.lib.CoreModule;
import de.jaschastarke.bukkit.tools.stats.IStatistics;
import de.jaschastarke.bukkit.tools.stats.MCStatsMetrics;
import de.jaschastarke.bukkit.tools.stats.MCStatsMetrics.Graph;
import de.jaschastarke.bukkit.tools.stats.PiwikStatistics;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;
import de.jaschastarke.modularize.ModuleEntry.ModuleState;

public class FeatureMetrics extends CoreModule<LimitedCreative> implements Listener {
    public FeatureMetrics(LimitedCreative plugin) {
        super(plugin);
    }
    private IStatistics metric;
    private MCStatsMetrics mcstats = null;
    
    @Override
    public void onEnable() {
        super.onEnable();
        metric = new PiwikStatistics(plugin);
        if (mcstats == null) {
            try {
                mcstats = new MCStatsMetrics(plugin);
                
                Graph moduleGraph = mcstats.createGraph("Module Usage");
                for (final ModuleEntry<IModule> mod : plugin.getModules()) {
                    if (mod.getModule() instanceof CoreModule<?>) {
                        moduleGraph.addPlotter(new MCStatsMetrics.Plotter(((CoreModule<?>) mod.getModule()).getName()) {
                            @Override
                            public int getValue() {
                                return mod.getState() == ModuleState.ENABLED ? 1 : 0;
                            }
                        });
                    }
                }
                Graph depGraph = mcstats.createGraph("Dependencies");
                for (final String dep : plugin.getDescription().getSoftDepend()) {
                    depGraph.addPlotter(new MCStatsMetrics.Plotter(dep) {
                        @Override
                        public int getValue() {
                            return plugin.getServer().getPluginManager().isPluginEnabled(dep) ? 1 : 0;
                        }
                    });
                }
                
                mcstats.start();
            } catch (IOException e) {
                // Failed to submit the stats :-(
                getLog().warn("MCStats-Error: " + e.getMessage());
            }
        }
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
