package de.jaschastarke.minecraft.limitedcreative;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
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
    public final static long TIME_OFFSET = 250;
    
    private List<BlockItemDrop> list = new ArrayList<BlockItemDrop>();
    
    public boolean isBlocked(Location l, Material type) {
        cleanup();
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
        if (isDebug());
            getLog().debug("  allowed");
        return false;
    }
    private void cleanup() {
        Iterator<BlockItemDrop> i = list.iterator();
        while (i.hasNext()) {
            BlockItemDrop block = i.next();
            if (block.getTimestamp() < System.currentTimeMillis() - TIME_OFFSET) {
                if (isDebug())
                    getLog().debug("Removing outdated BlokItemDrop: " + block.toString());
                i.remove();
            }
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
        if (player.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
            block(block.getLocation(), block.getType());
        } else {
            // doesn't include silktouch
            for (ItemStack i : block.getDrops(player.getItemInHand())) {
                block(block.getLocation(), i.getType());
            }
        }
    }

    public void block(Block block) {
        block(block, null);
    }
    public void block(Location l) {
        list.add(new BlockItemDrop(l, null));
    }
    public void block(Location l, Material type) {
        list.add(new BlockItemDrop(l, type));
    }
    
    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (event.isCancelled())
            return;
        if (event.getEntity() instanceof Item) {
            if (this.isBlocked(event.getLocation().getBlock().getLocation(), ((Item) event.getEntity()).getItemStack().getType())) {
                event.setCancelled(true);
            }
        }
    }
}
