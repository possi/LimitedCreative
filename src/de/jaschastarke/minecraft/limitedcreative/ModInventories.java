package de.jaschastarke.minecraft.limitedcreative;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import de.jaschastarke.bukkit.lib.Core;
import de.jaschastarke.bukkit.lib.CoreModule;
import de.jaschastarke.minecraft.limitedcreative.inventories.PlayerListener;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;

public class ModInventories extends CoreModule {
    public ModInventories(Core plugin) {
        super(plugin);
    }

    @Override
    public void Initialize(ModuleEntry<IModule> entry) {
        super.Initialize(entry);
        listeners.registerEvents(new PlayerListener(this));
    }
    
    public void onSetGameMode(Player player, GameMode gm) {
        //if (hasPermission(Perms.KEEPINVENTORY))
        //    return true;
        player.closeInventory();
        
        
        /*
        GameMode cgm = player.getGameMode();
        if (gm == GameMode.ADVENTURE && !plugin.config.getAdventureInv())
            gm = GameMode.SURVIVAL;
        if (cgm == GameMode.ADVENTURE && !plugin.config.getAdventureInv())
            cgm = GameMode.SURVIVAL;
        
        if (gm != cgm) {
            if (gm != GameMode.CREATIVE || plugin.config.getStoreCreative()) {
                getInv().save(cgm);
            }
            if (gm == GameMode.CREATIVE) {
                if (plugin.config.getStoreCreative() && getInv().isStored(GameMode.CREATIVE)) {
                    getInv().load(GameMode.CREATIVE);
                } else {
                    getInv().clear();
                }
                setCreativeArmor();
            } else if (gm == GameMode.SURVIVAL) {
                if (getInv().isStored(GameMode.SURVIVAL))
                    getInv().load(GameMode.SURVIVAL);
            } else if (gm == GameMode.ADVENTURE) {
                if (getInv().isStored(GameMode.ADVENTURE))
                    getInv().load(GameMode.ADVENTURE);
                else
                    getInv().clear();
            }
        }*/
    }
}
