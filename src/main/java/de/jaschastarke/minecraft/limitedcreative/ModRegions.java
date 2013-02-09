package de.jaschastarke.minecraft.limitedcreative;

import java.io.File;

import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import de.jaschastarke.bukkit.lib.CoreModule;
import de.jaschastarke.bukkit.lib.commands.AliasHelpedCommand;
import de.jaschastarke.minecraft.limitedcreative.regions.Flags;
import de.jaschastarke.minecraft.limitedcreative.regions.PlayerData;
import de.jaschastarke.minecraft.limitedcreative.regions.RegionConfig;
import de.jaschastarke.minecraft.limitedcreative.regions.RegionsCommand;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.CustomRegionManager;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.FlagList;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;
import de.jaschastarke.modularize.ModuleEntry.ModuleState;

public class ModRegions extends CoreModule<LimitedCreative> {
    private CustomRegionManager mgr;
    private WorldGuardPlugin wg;
    private PlayerData pdata;
    private FeatureBlockItemSpawn blockDrops = null;
    private RegionConfig config;
    private RegionsCommand command;
    
    public ModRegions(LimitedCreative plugin) {
        super(plugin);
    }
    
    @Override
    public void initialize(ModuleEntry<IModule> pEntry) {
        super.initialize(pEntry);
        
        blockDrops = plugin.getModule(FeatureBlockItemSpawn.class);
        if (blockDrops == null)
            blockDrops = plugin.addModule(new FeatureBlockItemSpawn(plugin)).getModule();
        
        config = plugin.getPluginConfig().registerSection(new RegionConfig(this, entry));
        
        command = new RegionsCommand(this);
        plugin.getCommandHandler().registerCommand(command);
        plugin.getMainCommand().registerCommand(new AliasHelpedCommand<RegionsCommand>(command, "region"));
        
        FlagList.addFlags(Flags.getList());
        
        if (!config.getEnabled()) {
            entry.initialState = ModuleState.DISABLED;
            return;
        }
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        
        mgr = new CustomRegionManager(new File(plugin.getDataFolder(), "regions.yml"), this);
        wg = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        pdata = new PlayerData(this);
        getLog().info(plugin.getLocale().trans("basic.loaded.module"));
    }
    
    public RegionConfig getConfig() {
        return config;
    }

    public WorldGuardPlugin getWorldGuard() {
        return wg;
    }
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
