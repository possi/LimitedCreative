package de.jaschastarke.minecraft.limitedcreative.inventories;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.jaschastarke.bukkit.lib.SimpleModule;
import de.jaschastarke.minecraft.limitedcreative.LimitedCreative;
import de.jaschastarke.minecraft.limitedcreative.ModInventories;
import fr.xephi.authme.events.RestoreInventoryEvent;
import fr.xephi.authme.events.StoreInventoryEvent;

public class AuthMe3Inventories extends SimpleModule<LimitedCreative> implements Listener {
    ModInventories invmod;
    public AuthMe3Inventories(LimitedCreative plugin, ModInventories modInventories) {
        super(plugin);
        invmod = modInventories;
    }

    @EventHandler
    public void onStoreInventory(StoreInventoryEvent event) {
        if (isDebug())
            getLog().debug("AuthMe Store Event: "+event.getPlayer().getName());
        
        event.getPlayer().closeInventory();
        GameMode cgm = event.getPlayer().getGameMode();
        
        if (cgm == GameMode.ADVENTURE && !invmod.getConfig().getSeparateAdventure())
            cgm = GameMode.SURVIVAL;
        
        if (cgm != GameMode.CREATIVE || invmod.getConfig().getStoreCreative()) {
            invmod.getInventory(event.getPlayer()).save(cgm);
        }
    }
    @EventHandler
    public void onRestoreInventory(RestoreInventoryEvent event) {
        if (isDebug())
            getLog().debug("AuthMe Restore Event: "+event.getPlayer().getName());
        
        event.getPlayer().closeInventory();
        GameMode cgm = event.getPlayer().getGameMode();
        
        if (cgm == GameMode.ADVENTURE && !invmod.getConfig().getSeparateAdventure())
            cgm = GameMode.SURVIVAL;
        
        Inventory inv = invmod.getInventory(event.getPlayer());
        if (inv.isStored(cgm)) {
            inv.load(cgm);
            event.setCancelled(true);
        }
    }
}
