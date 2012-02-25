package de.jaschastarke.minecraft.limitedcreative;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

abstract public class BlackList {
    
    abstract public boolean matches(Block b);
    abstract public boolean matches(ItemStack item);

    public static boolean isBlackListed(List<BlackList> list, Block block) {
        for (BlackList bl : list) {
            if (bl.matches(block))
                return true;
        }
        return false;
    }
    public static boolean isBlackListed(List<BlackList> list, ItemStack item) {
        for (BlackList bl : list) {
            if (bl.matches(item))
                return true;
        }
        return false;
    }
    
    public static class Some extends BlackList {
        private Material mat;
        private int data = -1;
        public Some(Material material, int data) {
            mat = material;
            this.data = data;
        }
        public Some(Material material) {
            mat = material;
        }
        public Some(MaterialData md) {
            mat = md.getItemType();
            if (md.getData() != 0)
                this.data = md.getData();
        }
        public boolean matches(Block block) {
            if (this.mat == null)
                return true;
            if (this.mat != block.getType())
                return false;
            if (this.data != -1 && this.data != block.getData())
                return false;
            return true;
        }
        public boolean matches(ItemStack item) {
            if (this.mat == null)
                return true;
            if (this.mat != item.getType())
                return false;
            if (this.data != -1) {
                if (this.mat == Material.MONSTER_EGG) {
                    if (this.data != item.getDurability())
                        return false;
                } else {
                    if (this.data != item.getData().getData())
                        return false;
                }
            }
            return true;
        }
    }
    
    public static class All extends BlackList {
        public boolean matches(Block b) {
            return true;
        }
        public boolean matches(ItemStack item) {
            return true;
        }
    }
}
