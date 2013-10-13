package de.jaschastarke.minecraft.limitedcreative.limits;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

import de.jaschastarke.minecraft.limitedcreative.ModCreativeLimits;

public class VehicleListener implements Listener {
    private ModCreativeLimits mod;
    public VehicleListener(ModCreativeLimits mod) {
        this.mod = mod;
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (event.getAttacker() instanceof Player) {
            Player player = (Player) event.getAttacker();
            if (player.getGameMode() == GameMode.CREATIVE && !mod.getPlugin().getPermManager().hasPermission(player, NoLimitPermissions.DROP)) {
                switch (event.getVehicle().getType()) {
                    case MINECART:
                        mod.getBlockSpawn().block(event.getVehicle().getLocation().getBlock().getLocation(), Material.MINECART);
                        break;
                    case MINECART_CHEST:
                        mod.getBlockSpawn().block(event.getVehicle().getLocation().getBlock().getLocation(), Material.STORAGE_MINECART);
                        break;
                    case MINECART_FURNACE:
                        mod.getBlockSpawn().block(event.getVehicle().getLocation().getBlock().getLocation(), Material.POWERED_MINECART);
                        break;
                    case MINECART_HOPPER:
                        mod.getBlockSpawn().block(event.getVehicle().getLocation().getBlock().getLocation(), Material.HOPPER_MINECART);
                        break;
                    case MINECART_TNT:
                        mod.getBlockSpawn().block(event.getVehicle().getLocation().getBlock().getLocation(), Material.EXPLOSIVE_MINECART);
                        break;
                    default:
                        break;
                    
                }
            }
        }
    }

}
