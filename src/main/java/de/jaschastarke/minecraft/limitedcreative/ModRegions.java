package de.jaschastarke.minecraft.limitedcreative;

import java.io.File;

import org.bukkit.entity.Player;

import de.jaschastarke.bukkit.lib.CoreModule;
import de.jaschastarke.bukkit.lib.commands.AliasHelpedCommand;
import de.jaschastarke.minecraft.limitedcreative.regions.BlockListener;
import de.jaschastarke.minecraft.limitedcreative.regions.IWorldGuardIntegration;
import de.jaschastarke.minecraft.limitedcreative.regions.PlayerData;
import de.jaschastarke.minecraft.limitedcreative.regions.PlayerListener;
import de.jaschastarke.minecraft.limitedcreative.regions.RegionConfig;
import de.jaschastarke.minecraft.limitedcreative.regions.RegionListener;
import de.jaschastarke.minecraft.limitedcreative.regions.RegionsCommand;
import de.jaschastarke.minecraft.limitedcreative.regions.WorldGuardIntegration;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.CustomRegionManager;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.PlayerRegionListener;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;
import de.jaschastarke.modularize.ModuleEntry.ModuleState;

public class ModRegions extends CoreModule<LimitedCreative> {
    private CustomRegionManager mgr;
    private IWorldGuardIntegration wg;
    private PlayerData pdata;
    private FeatureBlockItemSpawn blockDrops = null;
    private RegionConfig config;
    private RegionsCommand command;
    
    public ModRegions(LimitedCreative plugin) {
        super(plugin);
    }
    @Override
    public String getName() {
        return "Regions";
    }
    
    @Override
    public void initialize(ModuleEntry<IModule> pEntry) {
        super.initialize(pEntry);
        
        blockDrops = plugin.getModule(FeatureBlockItemSpawn.class);
        if (blockDrops == null)
            blockDrops = plugin.addModule(new FeatureBlockItemSpawn(plugin)).getModule();
        
        config = plugin.getPluginConfig().registerSection(new RegionConfig(this, entry));
        
        if (!plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            if (config.getEnabled())
                getLog().warn(plugin.getLocale().trans("region.warning.worldguard_not_found", getName()));
            entry.initialState = ModuleState.NOT_INITIALIZED;
            return;
        }
        
        command = new RegionsCommand(this);
        
        listeners.addListener(new PlayerListener(this));
        listeners.addListener(new BlockListener(this));
        listeners.addListener(new RegionListener(this));
        listeners.addListener(new PlayerRegionListener(this)); // Fires Custom-Events listen by RegionListener
        
        getWorldGuardIntegration().initFlagList();
    }
    
    public IWorldGuardIntegration getWorldGuardIntegration() {
        if (wg == null) {
            wg = new WorldGuardIntegration(this);
        }
        return wg;
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        
        mgr = new CustomRegionManager(new File(plugin.getDataFolder(), "regions.yml"), this);
        /*wg = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if (wg == null)
            throw new IllegalAccessError("Missing Plugin WorldGuard");*/

        plugin.getCommandHandler().registerCommand(command);
        plugin.getMainCommand().registerCommand(new AliasHelpedCommand<RegionsCommand>(command, "region", new String[]{"r"}));
        
        pdata = new PlayerData(this);
        getLog().info(plugin.getLocale().trans("basic.loaded.module"));
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
        
        plugin.getCommandHandler().removeCommand(command);
        plugin.getMainCommand().removeCommand(command);
    }

    public RegionConfig getConfig() {
        return config;
    }

    /*public WorldGuardPlugin getWorldGuard() {
        return wg;
    }*/
    public CustomRegionManager getRegionManager() {
        return mgr;
    }
    public PlayerData getPlayerData() {
        return pdata;
    }
    public PlayerData.Data getPlayerData(Player player) {
        return pdata.getData(player);
    }
    public FeatureBlockItemSpawn getBlockSpawn() {
        return blockDrops;
    }
}
