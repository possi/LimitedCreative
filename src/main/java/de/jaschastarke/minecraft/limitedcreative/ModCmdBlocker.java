package de.jaschastarke.minecraft.limitedcreative;

import de.jaschastarke.bukkit.lib.CoreModule;
import de.jaschastarke.minecraft.limitedcreative.cmdblocker.CmdBlockerConfig;
import de.jaschastarke.minecraft.limitedcreative.cmdblocker.CommandListener;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;

public class ModCmdBlocker extends CoreModule<LimitedCreative> {
    private CmdBlockerConfig config;

    public ModCmdBlocker(LimitedCreative plugin) {
        super(plugin);
    }
    @Override
    public String getName() {
        return "CmdBlocker";
    }

    @Override
    public void initialize(ModuleEntry<IModule> entry) {
        super.initialize(entry);
        listeners.addListener(new CommandListener(this));
        config = new CmdBlockerConfig(this, entry);
        plugin.getPluginConfig().registerSection(config);
    }
    @Override
    public void onEnable() {
        super.onEnable();
        getLog().info(plugin.getLocale().trans("basic.loaded.module"));
    }
    public CmdBlockerConfig getConfig() {
        return config;
    }
}
