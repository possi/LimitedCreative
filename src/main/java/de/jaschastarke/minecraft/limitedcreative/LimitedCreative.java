package de.jaschastarke.minecraft.limitedcreative;

import de.jaschastarke.I18n;
import de.jaschastarke.bukkit.lib.Core;
import de.jaschastarke.bukkit.lib.PluginLang;
import de.jaschastarke.bukkit.lib.configuration.ConfigCommand;

public class LimitedCreative extends Core {
    protected Config config = null;
    protected MainCommand command = null;
    
    @Override
    public void onInitialize() {
        super.onInitialize();
        config = new Config(this);
        
        setLang(new PluginLang("lang/messages", this));
        
        command = new MainCommand(this);
        ConfigCommand cc = new ConfigCommand(config, Permissions.CONFIG);
        cc.setPackageName(this.getName() + " - " + this.getLocale().trans(cc.getPackageName()));
        command.registerCommand(cc);
        commands.registerCommand(command);
        
        Hooks.inizializeHooks(this);
        
        addModule(new FeatureSwitchGameMode(this));
        addModule(new ModInventories(this));
        addModule(new ModCreativeLimits(this));
        addModule(new ModRegions(this));
        addModule(new ModCmdBlocker(this));
        addModule(new FeatureMetrics(this));
        
        config.saveDefault();
    }
    
    @Override
    public boolean isDebug() {
        return config.getDebug();
    }
    
    public Config getPluginConfig() {
        return config;
    }

    public I18n getLocale() {
        return getLang();
    }

    public MainCommand getMainCommand() {
        return command;
    }
}
