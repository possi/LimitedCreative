package de.jaschastarke.minecraft.limitedcreative.blockstate;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

import de.jaschastarke.minecraft.limitedcreative.ModBlockStates;
import de.jaschastarke.minecraft.limitedcreative.blockstate.worldedit.LCEditSessionFactory;

public class DependencyListener implements Listener {
    private ModBlockStates mod;

    public DependencyListener(ModBlockStates mod) {
        this.mod = mod;
    }
    
    @EventHandler
    public void onPluginLoaded(PluginEnableEvent event) {
        if (event.getPlugin().getName().equals("LogBlock") || event.getPlugin().getName().equals("WorldEdit")) {
            try {
                if (mod.getPlugin().getServer().getPluginManager().isPluginEnabled("WorldEdit"))
                    LCEditSessionFactory.initFactory(mod);
            } catch (Exception e) {
                mod.getLog().warn(mod.getPlugin().getLocale().trans("block_state.warning.worldedit_sessionfactory_failed", e.getMessage()));
            }
        }
    }
}
