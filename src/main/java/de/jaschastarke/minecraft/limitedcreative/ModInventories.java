package de.jaschastarke.minecraft.limitedcreative;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.jaschastarke.bukkit.lib.CoreModule;
import de.jaschastarke.minecraft.limitedcreative.inventories.ArmoryConfig;
import de.jaschastarke.minecraft.limitedcreative.inventories.Inventory;
import de.jaschastarke.minecraft.limitedcreative.inventories.InventoryConfig;
import de.jaschastarke.minecraft.limitedcreative.inventories.InventoryPermissions;
import de.jaschastarke.minecraft.limitedcreative.inventories.PlayerListener;
import de.jaschastarke.minecraft.limitedcreative.inventories.store.InvYamlStorage;
import de.jaschastarke.minecraft.limitedcreative.inventories.store.PlayerInventoryStorage;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;

public class ModInventories extends CoreModule<LimitedCreative> {
    protected PlayerInventoryStorage storage;
    protected Map<Player, Inventory> inventories;
    protected InventoryConfig config;
    protected ArmoryConfig armor_config;
    
    public ModInventories(LimitedCreative plugin) {
        super(plugin);
    }
    @Override
    public String getName() {
        return "Inventory";
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initialize(ModuleEntry<IModule> entry) {
        super.initialize(entry);
        listeners.addListener(new PlayerListener(this));
        config = plugin.getPluginConfig().registerSection(new InventoryConfig(this, entry));
        armor_config = config.registerSection(new ArmoryConfig(this));
        
        if (Hooks.isAuthMePresent()) {
            addModule(new de.jaschastarke.minecraft.limitedcreative.inventories.AuthMeInventories(plugin, this));
        }
        String incomp = Hooks.InventoryIncompatible.test();
        if (config.getEnabled() && incomp != null) {
            getLog().warn(plugin.getLocale().trans("inventory.warning.conflict", incomp, this.getName()));
            entry.deactivateUsage();
        }
    }
    @Override
    public void onEnable() {
        String incomp = Hooks.InventoryIncompatible.test();
        if (incomp != null) {
            throw new IllegalAccessError(plugin.getLocale().trans("inventory.warning.conflict", incomp, this.getName()));
        }
        super.onEnable();
        storage = new InvYamlStorage(this, new File(plugin.getDataFolder(), config.getFolder()));
        inventories = new WeakHashMap<Player, Inventory>();
        getLog().info(plugin.getLocale().trans("basic.loaded.module"));
    }
    public InventoryConfig getConfig() {
        return config;
    }
    public ArmoryConfig getArmorConfig() {
        return armor_config;
    }
    
    public PlayerInventoryStorage getStorage() {
        return storage;
    }
    
    public Inventory getInventory(Player player) {
        if (inventories.containsKey(player)) {
            return inventories.get(player);
        } else {
            Inventory inv = new Inventory(storage, player);
            inventories.put(player, inv);
            return inv;
        }
    }

    public void onSetGameMode(Player player, GameMode gm) {
        if (plugin.getPermManager().hasPermission(player, InventoryPermissions.KEEP_INVENTORY))
            return;
        player.closeInventory();
        
        GameMode cgm = player.getGameMode();
        if (gm == GameMode.ADVENTURE && !config.getSeparateAdventure())
            gm = GameMode.SURVIVAL;
        else if (gm == GameMode.SPECTATOR)
            gm = GameMode.CREATIVE;
        if (cgm == GameMode.ADVENTURE && !config.getSeparateAdventure())
            cgm = GameMode.SURVIVAL;
        else if (cgm == GameMode.SPECTATOR)
            cgm = GameMode.CREATIVE;
        
        if (gm != cgm) {
            if (gm != GameMode.CREATIVE || config.getStoreCreative()) {
                getInventory(player).save(cgm);
            }
            if (gm == GameMode.CREATIVE) {
                if (config.getStoreCreative() && getInventory(player).isStored(GameMode.CREATIVE)) {
                    getInventory(player).load(GameMode.CREATIVE);
                } else {
                    getInventory(player).clear();
                }
                setCreativeArmor(player);
            } else if (gm == GameMode.SURVIVAL) {
                if (getInventory(player).isStored(GameMode.SURVIVAL))
                    getInventory(player).load(GameMode.SURVIVAL);
            } else if (gm == GameMode.ADVENTURE) {
                if (getInventory(player).isStored(GameMode.ADVENTURE))
                    getInventory(player).load(GameMode.ADVENTURE);
                else
                    getInventory(player).clear();
            }
        }
    }
    
    public void setCreativeArmor(Player player) {
        if (!getPlugin().getPermManager().hasPermission(player, InventoryPermissions.BYPASS_CREATIVE_ARMOR)) {
            Map<String, ItemStack> armor = armor_config.getCreativeArmor();
            if (armor != null) {
                ItemStack[] is = new ItemStack[4];
                if (armor.containsKey("feet"))
                    is[0] = armor.get("feet");
                if (armor.containsKey("legs"))
                    is[1] = armor.get("legs");
                if (armor.containsKey("chest"))
                    is[2] = armor.get("chest");
                if (armor.containsKey("head"))
                    is[3] = armor.get("head");
                player.getInventory().setArmorContents(is);
            }
        }
    }
}
