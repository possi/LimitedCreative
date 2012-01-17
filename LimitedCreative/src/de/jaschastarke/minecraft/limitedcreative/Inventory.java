package de.jaschastarke.minecraft.limitedcreative;

import java.io.File;
import java.io.IOException;

import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import de.jaschastarke.minecraft.limitedcreative.serialize.Armor;
import de.jaschastarke.minecraft.limitedcreative.serialize.Items;

public class Inventory {
    protected Player player;
    protected PlayerInventory inv;
    
    public Inventory(Player p) {
        player = p;
        inv = p.getInventory();
    }
    
    public void save() {
        File f = new File(LimitedCreativeCore.plugin.getDataFolder(), getFileName(player, player.getGameMode()));
        storeInventory(inv, f);
    }
    
    public void load(GameMode gm) {
        File f = new File(LimitedCreativeCore.plugin.getDataFolder(), getFileName(player, gm));
        restoreInventory(inv, f);
    }
    public void load() {
        load(player.getGameMode());
    }
    
    public void clear() {
        inv.setArmorContents(new ItemStack[]{
            new ItemStack(0),
            new ItemStack(0),
            new ItemStack(0),
            new ItemStack(0),
        });
        inv.clear();
    }
    
    private String getFileName(Player player, GameMode gm) {
        if (gm != GameMode.SURVIVAL) {
            return LimitedCreativeCore.plugin.config.getInventoryFolder() + File.separator + player.getName()+"_"+gm.toString()+".yml";
        } else {
            return LimitedCreativeCore.plugin.config.getInventoryFolder() + File.separator + player.getName()+".yml";
        }
    }
    
    protected static void storeInventory(PlayerInventory pinv, File file) {
        YamlConfiguration yml = new YamlConfiguration();
        
        new Armor(pinv).store(yml.createSection("armor"));
        new Items(pinv).store(yml.createSection("inv"));
        
        try {
            yml.save(file);
        } catch (IOException e) {
            LimitedCreativeCore.plugin.logger.severe(e.getMessage());
        }
    }
    protected static void restoreInventory(PlayerInventory pinv, File file) {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        
        new Armor(pinv).restore(yml.getConfigurationSection("armor"));
        new Items(pinv).restore(yml.getConfigurationSection("inv"));
    }

    public boolean isStored(GameMode gm) {
        File f = new File(LimitedCreativeCore.plugin.getDataFolder(), getFileName(player, gm));
        return f.exists();
    }
}
