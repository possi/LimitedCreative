package de.jaschastarke.minecraft.limitedcreative;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class PlayerCore {
    private static LimitedCreativeCore plugin;
    private Player player;

    public PlayerCore(LimitedCreativeCore pplugin, Player pplayer) {
        plugin = pplugin;
        player = pplayer;
    }
    
    public void onSetCreative() {
        Inventory inv = new Inventory(player);
        inv.save();
        if (plugin.config.getStoreCreative() && inv.isStored(GameMode.CREATIVE)) {
            inv.load(GameMode.CREATIVE);
        } else {
            inv.clear();
        }
    }
    public void onSetSurvival() {
        Inventory inv = new Inventory(player);
        if (plugin.config.getStoreCreative()) {
            inv.save();
        }
        if (inv.isStored(GameMode.SURVIVAL))
            inv.load(GameMode.SURVIVAL);
    }
}
