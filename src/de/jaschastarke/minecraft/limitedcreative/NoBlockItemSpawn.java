/*
 * Limited Creative - (Bukkit Plugin)
 * Copyright (C) 2012 jascha@ja-s.de
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.jaschastarke.minecraft.limitedcreative;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;


/**
 * The "Block" means a Minecraft-Block, not "blocking". So this Class is used to prevent ItemSpawn which are Drops from
 * specified Blocks.
 */
public class NoBlockItemSpawn {
    public final static long TIME_OFFSET = 250;
    
    private List<BlockItemDrop> list = new ArrayList<BlockItemDrop>();
    
    public boolean isBlocked(Location l, Material type) {
        cleanup();
        for (BlockItemDrop block : list) {
            if (block.getLocation().equals(l) && block.getType().equals(type))
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
    
    public void block(Block block, LCPlayer player) {
        if (player.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
            block(block.getLocation(), block.getType());
        } else {
            // doesn't include silktouch
            for (ItemStack i : block.getDrops(player.getPlayer().getItemInHand())) {
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
}
