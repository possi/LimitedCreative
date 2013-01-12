package de.jaschastarke.minecraft.integration;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import uk.org.whoami.authme.events.RestoreInventoryEvent;
import uk.org.whoami.authme.events.StoreInventoryEvent;

import de.jaschastarke.minecraft.limitedcreative.Core;
import de.jaschastarke.minecraft.limitedcreative.LCPlayer;
import de.jaschastarke.minecraft.limitedcreative.Players;

public class AuthMeInventories implements Listener {
    public AuthMeInventories(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void onStoreInventory(StoreInventoryEvent event) {
        Core.debug("AuthMe Store Event: "+event.getPlayer().getName());
        LCPlayer player = Players.get(event.getPlayer());
        
        event.getPlayer().closeInventory();
        GameMode cgm = event.getPlayer().getGameMode();
        
        if (cgm == GameMode.ADVENTURE && !Core.plugin.config.getAdventureInv())
            cgm = GameMode.SURVIVAL;
        
        if (cgm != GameMode.CREATIVE || Core.plugin.config.getStoreCreative()) {
            player.getInv().save(cgm);
        }
    }
    @EventHandler
    public void onRestoreInventory(RestoreInventoryEvent event) {
        Core.debug("AuthMe Restore Event: "+event.getPlayer().getName());
        LCPlayer player = Players.get(event.getPlayer());
        
        event.getPlayer().closeInventory();
        GameMode cgm = event.getPlayer().getGameMode();
        
        if (cgm == GameMode.ADVENTURE && !Core.plugin.config.getAdventureInv())
            cgm = GameMode.SURVIVAL;
        
        if (player.getInv().isStored(cgm)) {
            player.getInv().load(cgm);
            event.setCancelled(true);
        }
    }
}
