package de.jaschastarke.minecraft.limitedcreative;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

import de.jaschastarke.modularize.ModuleEntry.ModuleState;

/**
 * @Todo may be abstract to some per-module-definitions that are checked onEnabled and here automaticly
 */
public class DependencyListener implements Listener {
    private LimitedCreative plugin;

    public DependencyListener(LimitedCreative plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPluginLoaded(PluginEnableEvent event) {
        ModInventories mod = plugin.getModule(ModInventories.class);
        if (mod != null && mod.getModuleEntry().getState() == ModuleState.ENABLED) {
            String incomp = Hooks.InventoryIncompatible.test();
            if (incomp != null) {
                mod.getLog().warn(plugin.getLocale().trans("inventory.warning.conflict", incomp, mod.getName()));
                mod.getModuleEntry().deactivateUsage();
            }
        }
    }
    
    @EventHandler
    public void onPluginUnloaded(PluginDisableEvent event) {
        if (event.getPlugin().getName().equals("Vault")) {
            ModGameModePerm mod = plugin.getModule(ModGameModePerm.class);
            if (mod != null && mod.getModuleEntry().getState() == ModuleState.ENABLED) {
                mod.getLog().warn(plugin.getLocale().trans("gmperm.warning.vault_not_found", mod.getName()));
                mod.getModuleEntry().deactivateUsage();
            }
        } else if (event.getPlugin().getName().equals("WorldGuard")) {
            ModRegions mod = plugin.getModule(ModRegions.class);
            if (mod != null && mod.getModuleEntry().getState() == ModuleState.ENABLED) {
                mod.getLog().warn(plugin.getLocale().trans("region.warning.worldguard_not_found", mod.getName()));
                mod.getModuleEntry().deactivateUsage();
            }
        }
    }
}
