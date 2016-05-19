package de.jaschastarke.minecraft.limitedcreative;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

import de.jaschastarke.bukkit.lib.CoreModule;

public class FeatureBlockItemSpawn extends CoreModule<LimitedCreative> implements Listener {
    public FeatureBlockItemSpawn(LimitedCreative plugin) {
        super(plugin);
    }
    private CleanUp cleanup = new CleanUp();
    public final static long TICK_OFFSET = 1;
    
    private List<BlockItemDrop> list = new ArrayList<BlockItemDrop>();
    
    public boolean isBlocked(Location l, Material type) {
        if (isDebug())
            getLog().debug("Checking ItemBlocked: " + l.toString() + " - " + type.toString());
        for (BlockItemDrop block : list) {
            if (isDebug())
                getLog().debug("  - " + block.toString());
            if (block.getLocation().equals(l) && (block.getType() == null || block.getType().equals(type))) {
                if (isDebug())
                    getLog().debug("  blocked!");
                return true;
            }
        }
        if (isDebug())
            getLog().debug("  allowed");
        return false;
    }
    
    private void scheduleCleanUp() {
        if (cleanup.maxTime == 0) { // if not scheduled yet
            cleanup.maxTime = System.currentTimeMillis();
            Bukkit.getScheduler().runTaskLater(plugin, cleanup, TICK_OFFSET);
        }
    }

    private class BlockItemDrop {
        public BlockItemDrop(Location l, Material type) {
            this.l = l;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }
        private Location l;
        private Material type;
        private long timestamp;
        public Location getLocation() {
            return l;
        }
        public Material getType() {
            return type;
        }
        public long getTimestamp() {
            return timestamp;
        }
        public String toString() {
            return Long.toString(timestamp) + ": " + l.toString() + (type != null ? " - " + type.toString() : "");
        }
    }
    
    public void block(Block block, Player player) {
        if (player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
            block(block.getLocation(), block.getType());
        } else {
            // doesn't include silktouch
            for (ItemStack i : block.getDrops(player.getInventory().getItemInMainHand())) {
                block(block.getLocation(), i.getType());
            }
        }
    }

    public void block(Block block) {
        block(block.getLocation());
    }
    public void block(Location l) {
        list.add(new BlockItemDrop(l, null));
        scheduleCleanUp();
    }
    public void block(Location l, Material type) {
        list.add(new BlockItemDrop(l, type));
        scheduleCleanUp();
    }
    public void block(Location l, ItemStack item) {
        if (item != null)
            block(l, item.getType());
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        if (event.getEntity() != null) {
            if (this.isBlocked(event.getLocation().getBlock().getLocation(), event.getEntity().getItemStack().getType())) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Don't default Plugin-debug to this mod. Because it is too spammy.
     */
    public boolean isDebug() {
        return debug;
    }
    
    private class CleanUp implements Runnable {
        public long maxTime = 0;

        @Override
        public void run() {
            if (plugin.isDebug())
                plugin.getLog().debug("Scheduler: Synchronous Task run: Cleanup");
            Iterator<BlockItemDrop> i = list.iterator();
            while (i.hasNext()) {
                BlockItemDrop block = i.next();
                if (block.getTimestamp() <= maxTime) {
                    if (isDebug())
                        getLog().debug("Removing outdated BlokItemDrop: " + block.toString());
                    i.remove();
                }
            }
            
            maxTime = 0;
            if (list.size() > 0)
                scheduleCleanUp();
        }
    }
}
