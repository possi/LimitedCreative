package de.jaschastarke.minecraft.limitedcreative;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.RegisteredServiceProvider;

import de.jaschastarke.bukkit.lib.CoreModule;
import de.jaschastarke.minecraft.limitedcreative.gmperm.GMPermConfig;
import de.jaschastarke.minecraft.limitedcreative.gmperm.PlayerListener;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;
import de.jaschastarke.modularize.ModuleEntry.ModuleState;

public class ModGameModePerm extends CoreModule<LimitedCreative> {
    private GMPermConfig config;
    private Permission permission = null;
    private RegisteredServiceProvider<Permission> permissionProvider;

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
        
        permissionProvider = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        
        if (config.getEnabled()) {
            if (!plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
                getLog().warn(plugin.getLocale().trans("gmperm.warning.vault_not_found", getName()));
                entry.initialState = ModuleState.NOT_INITIALIZED;
            } /*else if (!getVaultPermission().hasGroupSupport()) {
                getLog().warn(plugin.getLocale().trans("gmperm.warning.no_group_support", getName()));
                entry.initialState = ModuleState.NOT_INITIALIZED;
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
        permission = null;
    }
    public GMPermConfig getConfig() {
        return config;
    }
    public Permission getVaultPermission() {
        if (permissionProvider != null && permission == null) {
            permission = permissionProvider.getProvider();
        }
        return permission;
    }
}
