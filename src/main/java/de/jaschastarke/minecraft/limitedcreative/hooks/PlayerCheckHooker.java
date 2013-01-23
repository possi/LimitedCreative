package de.jaschastarke.minecraft.limitedcreative.hooks;

import org.bukkit.entity.Player;

import de.jaschastarke.hooking.AbstractHooker;

public class PlayerCheckHooker extends AbstractHooker<PlayerCheckHooker.Check> {
    public interface Check {
        boolean test(Player player);
    }
    
    protected boolean def;
    public PlayerCheckHooker(boolean defaultValue) {
        def = defaultValue;
    }
    public boolean test(Player player) {
        for (Check c : hooks) {
            boolean ret = c.test(player);
            if (ret != def)
                return ret;
        }
        return def;
    }
}
