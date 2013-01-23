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
        for (BlockItemDrop block : list) {
            if (block.getLocation().equals(l) && (block.getType() == null || block.getType().equals(type)))
                return true;
        }
        return false;
    }
    private void cleanup() {
        Iterator<BlockItemDrop> i = list.iterator();
        while (i.hasNext()) {
            BlockItemDrop block = i.next();
            if (block.getTimestamp() < System.currentTimeMillis() - TIME_OFFSET)
                i.remove();
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
