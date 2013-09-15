package de.jaschastarke.minecraft.limitedcreative.regions;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import de.jaschastarke.minecraft.limitedcreative.ModRegions;

public class PlayerData {
    private static final String DEFAULT_FILENAME = "players.yml";
    private ModRegions mod;
    private File file;
    private YamlConfiguration data;
    private WeakHashMap<Player, Data> players = new WeakHashMap<Player, PlayerData.Data>();
    
    public PlayerData(ModRegions mod) {
        this(mod, new File(mod.getPlugin().getDataFolder(), DEFAULT_FILENAME));
    }
    public PlayerData(ModRegions mod, File yamlFile) {
        this.mod = mod;
        this.file = yamlFile;
        if (yamlFile.exists())
            this.data = YamlConfiguration.loadConfiguration(file);
        else
            this.data = new YamlConfiguration();
        this.data.options().header("DO NOT MODIFY THIS FILE");
    }
    
    protected ConfigurationSection getStorage() {
        return data;
    }
    
    public Data getData(final Player player) {
        if (players.containsKey(player)) {
            return players.get(player);
        } else {
            Data pdata = new Data(player);
            players.put(player, pdata);
            return pdata;
        }
    }
    
    protected void save() {
        try {
            data.save(file);
        } catch (IOException e) {
            mod.getPlugin().getLogger().severe("Failed to save " + file.getName());
            e.printStackTrace();
        }
    }

    public void clearAllTemp() {
        for (Data data : players.values()) {
            data.clearTemp();
        }
    }
    
    private ConfigurationSection getSect(final String player) {
        if (data.contains(player) && data.isConfigurationSection(player)) {
            return data.getConfigurationSection(player);
        } else {
            return data.createSection(player);
        }
    }
    
    public class Data {
        private WeakReference<Player> player;
        private String currentHash;
        
        private Data(final Player player) {
            this.player = new WeakReference<Player>(player);
        }
        private String n() {
            return player.get().getName();
        }
        
        public GameMode getActiveRegionGameMode() {
            return getGameMode("region_gamemode");
        }
        public void storeActiveRegionGameMode(GameMode regionGameMode) {
            getSect(n()).set("region_gamemode", regionGameMode != null ? regionGameMode.toString() : null);
            cleanUp();
            save();
        }
        public GameMode getPermanentRegionGameMode() {
            return getGameMode("permanent_gamemode");
        }
        public void storePermanentGameMode(GameMode currentGameMode) {
            getSect(n()).set("permanent_gamemode", currentGameMode != null ? currentGameMode.toString() : null);
            cleanUp();
            save();
        }
        public GameMode getOptionalRegionGameMode(String regionHash) {
            return getGameMode("optional_gamemode." + regionHash);
        }
        public void setOptionalRegionGameMode(String regionHash, GameMode currentGameMode) {
            getSect(n()+".optional_gamemode").set(regionHash, currentGameMode != null ? currentGameMode.toString() : null);
            cleanUp();
            save();
        }
        
        private GameMode getGameMode(String path) {
            if (getSect(n()).contains(path) && getSect(n()).isString(path)) {
                return GameMode.valueOf(getSect(n()).getString(path));
            } else if (getSect(n()).contains(path)) {
                return (GameMode) getSect(n()).get(path);
            } else {
                return null;
            }
        }
        
        private void cleanUp() {
            if (data.contains(n()) && data.isConfigurationSection(n())) {
                if (data.getConfigurationSection(n()).getKeys(false).size() == 0) {
                    remove();
                }
            }
        }
        public void remove() {
            data.set(n(), null);
        }
        
        public void setHash(String hash) {
            currentHash = hash;
        }
        public String getHash() {
            return currentHash;
        }
        public void clearTemp() {
            currentHash = null;
        }
    }
}
