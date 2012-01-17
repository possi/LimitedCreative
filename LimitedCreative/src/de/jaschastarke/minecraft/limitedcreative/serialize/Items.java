package de.jaschastarke.minecraft.limitedcreative.serialize;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.PlayerInventory;

public class Items implements Storeable {
    private PlayerInventory inv;
    public Items(PlayerInventory pi) {
        inv = pi;
    }

    @Override
    public void store(ConfigurationSection section) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) != null && inv.getItem(i).getTypeId() != 0)
                section.set(String.valueOf(i), inv.getItem(i));
        }
    }

    @Override
    public void restore(ConfigurationSection section) {
        inv.clear();
        for (int i = 0; i < inv.getSize(); i++) {
            if (section.contains(String.valueOf(i))) {
                inv.setItem(i, section.getItemStack(String.valueOf(i)));
            }
        }
    }

}
