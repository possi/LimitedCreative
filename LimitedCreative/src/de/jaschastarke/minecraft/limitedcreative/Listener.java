package de.jaschastarke.minecraft.limitedcreative;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.plugin.PluginManager;

public final class Listener {
    private static LimitedCreativeCore plugin;
    private static PluginManager pm;
    
    public static class PlayerListen extends PlayerListener {
        @Override
        public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
            if (event.getNewGameMode() == GameMode.CREATIVE) {
                new PlayerCore(plugin, event.getPlayer()).onSetCreative();
            } else if (event.getNewGameMode() == GameMode.SURVIVAL) {
                new PlayerCore(plugin, event.getPlayer()).onSetSurvival();
            }
        }
        

        @Override
        public void onPlayerDropItem(PlayerDropItemEvent event) {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE && !plugin.config.getDropInCreative()) {
                event.setCancelled(true);
            }
        }

        @Override
        public void onPlayerPickupItem(PlayerPickupItemEvent event) {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE && !plugin.config.getDropInCreative()) {
                event.setCancelled(true);
            }
        } 

        @Override
        public void onPlayerInteract(PlayerInteractEvent event) {
            if (event.isCancelled() || event.getPlayer().getGameMode() == GameMode.SURVIVAL)
                return;

            if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;
            
            Block block = event.getClickedBlock();
            
            if (block.getState() instanceof ContainerBlock) {
                event.getPlayer().sendMessage("Access to chests is not allowed in creative mode");
                event.setCancelled(true);
            }
            if (plugin.config.getSignBlock() && block.getState() instanceof Sign) {
                event.getPlayer().sendMessage("Access to interact with signs is not allowed in creative mode");
                event.setCancelled(true);
            }
        }

        @Override
        public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
            if (event.isCancelled() || event.getPlayer().getGameMode() == GameMode.SURVIVAL)
                return;
            
            Entity entity = event.getRightClicked();

            if (entity instanceof StorageMinecart) {
                event.getPlayer().sendMessage("Access to chests is not allowed in creative mode");
                event.setCancelled(true);
            }
        }
        
        private void register() {
            pm.registerEvent(Event.Type.PLAYER_GAME_MODE_CHANGE, this, Priority.Normal, plugin);
            pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, this, Priority.Normal, plugin);
            pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, this, Priority.Normal, plugin);
            pm.registerEvent(Event.Type.PLAYER_INTERACT, this, Priority.Lowest, plugin);
            pm.registerEvent(Event.Type.PLAYER_INTERACT_ENTITY, this, Priority.Lowest, plugin);
        }
    }
    
    public static class EntityListen extends EntityListener {
        @Override
        public void onEntityDamage(EntityDamageEvent meta_event) {
            if (meta_event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) meta_event;
                if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
                    // its PVP
                    if (((Player) event.getEntity()).getGameMode() == GameMode.CREATIVE ||
                        ((Player) event.getDamager()).getGameMode() == GameMode.CREATIVE) { // one of them is creative
                        event.setCancelled(true);
                    }
                }
            }
        }

        private void register() {
            pm.registerEvent(Event.Type.ENTITY_DAMAGE, this, Priority.Normal, plugin);
        }
    }
    
    public static void register(LimitedCreativeCore pplugin) {
        plugin = pplugin;
        pm = plugin.getServer().getPluginManager();
        
        new PlayerListen().register();
        new EntityListen().register();
    }
}
