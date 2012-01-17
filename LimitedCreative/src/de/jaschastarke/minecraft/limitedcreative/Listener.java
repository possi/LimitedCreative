package de.jaschastarke.minecraft.limitedcreative;
import org.bukkit.GameMode;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
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

        private void register() {
            pm.registerEvent(Event.Type.PLAYER_GAME_MODE_CHANGE, this, Priority.Low, plugin);
            pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, this, Priority.High, plugin);
            pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, this, Priority.High, plugin);
        }
    }

    public static void register(LimitedCreativeCore pplugin) {
        plugin = pplugin;
        pm = plugin.getServer().getPluginManager();
        
        new PlayerListen().register();
    }
}
