package de.jaschastarke.minecraft.limitedcreative.limits;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.entity.Entity;

public class EntityNoDrop {
    private Map<Entity, Prevent> nodrop = new WeakHashMap<Entity, Prevent>();
    
    private Prevent get(Entity entity) {
        if (!nodrop.containsKey(entity))
            nodrop.put(entity, new Prevent());
        return nodrop.get(entity);
    }
    
    public void preventXP(Entity entity) {
        get(entity).xp = true;
    }
    public void preventDrop(Entity entity) {
        get(entity).drops = true;
    }
    public boolean isXPPrevented(Entity entity) {
        return nodrop.containsKey(entity) && nodrop.get(entity).xp;
    }
    public boolean isDropPrevented(Entity entity) {
        return nodrop.containsKey(entity) && nodrop.get(entity).drops;
    }
    public void remove(Entity entity) {
        nodrop.remove(entity);
    }
    
    private class Prevent {
        boolean xp = false;
        boolean drops = false;
    }
}
