package de.jaschastarke.minecraft.limitedcreative;

import de.jaschastarke.bukkit.lib.CoreModule;
import de.jaschastarke.minecraft.limitedcreative.gmperm.GMPermConfig;
import de.jaschastarke.minecraft.limitedcreative.gmperm.PermissionInterface;
import de.jaschastarke.minecraft.limitedcreative.gmperm.PlayerListener;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;
import de.jaschastarke.modularize.ModuleEntry.ModuleState;

public class ModGameModePerm extends CoreModule<LimitedCreative> {
    private GMPermConfig config;
    private PermissionInterface permission = null;

    public ModGameModePerm(LimitedCreative plugin) {
        super(plugin);
    }
    @Override
    public String getName() {
        return "GameModePerm";
    }

    @Override
    public void initialize(ModuleEntry<IModule> entry) {
        super.initialize(entry);
        listeners.addListener(new PlayerListener(this));
        config = new GMPermConfig(this, entry);
        plugin.getPluginConfig().registerSection(config);
        
        if (!plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
            if (config.getEnabled())
                getLog().warn(plugin.getLocale().trans("gmperm.warning.vault_not_found", getName()));
            entry.initialState = ModuleState.NOT_INITIALIZED;
            return;
        }
        
        permission = new PermissionInterface(this);
        
        if (config.getEnabled()) {
            if (!permission.isPresent()) {
                getLog().warn(plugin.getLocale().trans("gmperm.warning.vault_not_found", getName()));
                entry.initialState = ModuleState.NOT_INITIALIZED;
                return;
            } /*else if (!getVaultPermission().hasGroupSupport()) {
                getLog().warn(plugin.getLocale().trans("gmperm.warning.no_group_support", getName()));
                entry.initialState = ModuleState.NOT_INITIALIZED;
                return;
            }*/
        }
    }
    @Override
    public void onEnable() {
        super.onEnable();
        
        getLog().info(plugin.getLocale().trans("basic.loaded.module"));
    }
    @Override
    public void onDisable() {
        super.onDisable();
        permission.clear();
    }
    public GMPermConfig getConfig() {
        return config;
    }
    public PermissionInterface getPermissionInterface() {
        return permission;
    }
}
