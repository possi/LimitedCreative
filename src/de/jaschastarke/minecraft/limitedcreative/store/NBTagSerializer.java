package de.jaschastarke.minecraft.limitedcreative.store;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.NBTTagCompound;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class NBTagSerializer {
    
    public static Map<String, Object> serializeTags(ItemStack cis) {
        Map<String, Object> map = null;
        NBTTagCompound tag = getTag(cis);
        if (tag != null && tag.getCompound("display") != null) {
            NBTTagCompound display = tag.getCompound("display");
            if (display.hasKey("Name")) {
                map = new HashMap<String, Object>();
                map.put("name", display.getString("Name"));
            }
        }
        return map;
    }
    
    public static ItemStack unserializeTags(ItemStack cis, Map<String, Object> data) {
        if (data.size() > 0) {
            if (!(cis instanceof CraftItemStack)) {
                cis = new CraftItemStack(cis);
            }
            NBTTagCompound nbt = getTag(cis, true);
            
            if (data.containsKey("name")) {
                NBTTagCompound display;
                if (nbt.hasKey("display")) {
                    display = nbt.getCompound("display");
                } else {
                    display = new NBTTagCompound("display");
                    nbt.setCompound("display", display);
                }
                display.setString("Name", (String) data.get("name"));
            }
        }
        return cis;
    }
    
    private static NBTTagCompound getTag(ItemStack stack, boolean force) {
        NBTTagCompound nbt = getTag(stack);
        if (nbt == null && force) {
            nbt = new NBTTagCompound();
            getMCItemStack(stack).setTag(nbt);
        }
        return nbt;
    }
    
    private static NBTTagCompound getTag(ItemStack stack) {
        net.minecraft.server.ItemStack is = getMCItemStack(stack);
        if (is != null) {
            return is.getTag();
        } else {
            return null;
        }
    }
    
    private static net.minecraft.server.ItemStack getMCItemStack(ItemStack stack) {
        if (stack instanceof CraftItemStack) {
            return getMCItemStack((CraftItemStack) stack);
        } else {
            return null;
        }
    }
    private static net.minecraft.server.ItemStack getMCItemStack(CraftItemStack stack) {
        return stack.getHandle();
    }
}
