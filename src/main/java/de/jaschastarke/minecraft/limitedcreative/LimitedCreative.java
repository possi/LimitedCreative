package de.jaschastarke.minecraft.limitedcreative;

import de.jaschastarke.i18n;
import de.jaschastarke.bukkit.lib.Core;
import de.jaschastarke.bukkit.lib.PluginLang;

public class LimitedCreative extends Core {
    protected Config config = null;
    protected MainCommand command = null;
    
    @Override
    public void OnInitialize() {
        super.OnInitialize();
        config = new Config(this);
        this.debug = config.getDebug();
        
        setLang(new PluginLang("lang/messages", this));
        
        command = new MainCommand(this);
        commands.registerCommand(command);
        
        Hooks.inizializeHooks(this);
        
        addModule(new FeatureSwitchGameMode(this));
        addModule(new ModInventories(this));
        addModule(new ModCreativeLimits(this));
        addModule(new ModRegions(this));
        addModule(new ModCmdBlocker(this));
        
        config.saveDefault();
    }
    
    public Config getPluginConfig() {
        return config;
    }

    public i18n getLocale() {
        return getLang();
    }

    public MainCommand getMainCommand() {
        return command;
    }
}
