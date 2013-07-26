package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Embeddable;
import javax.persistence.Entity;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

@Embeddable
@Entity
@Deprecated
public class BlockLocation implements Serializable {
    private static final long serialVersionUID = -8644798679923736348L;

    private int x;
    
    private int y;
    
    private int z;
    
    private UUID world;
    
    public BlockLocation() {
    }
    public BlockLocation(Location loc) {
        setLocation(loc);
    }

    public UUID getWorld() {
        return world;
    }
    public World getWorldObject() {
        return Bukkit.getWorld(getWorld());
    }

    public void setWorld(UUID world) {
        this.world = world;
    }

    public void setWorld(World world) {
        setWorld(world.getUID());
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }
    
    public void setLocation(Location loc) {
        setWorld(loc.getWorld());
        setX(loc.getBlockX());
        setY(loc.getBlockY());
        setZ(loc.getBlockZ());
    }
    public Location getLocation() {
        return new Location(Bukkit.getWorld(getWorld()), getX(), getY(), getZ());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockLocation) {
            return world.equals(((BlockLocation) obj).world) &&
                    x == ((BlockLocation) obj).x &&
                    y == ((BlockLocation) obj).y &&
                    z == ((BlockLocation) obj).z;
        }
        return super.equals(obj);
    }
    @Override
    public int hashCode() {
        return (((x * 13) + y) * 7 + z) * 23 + world.hashCode();
    }
    @Override
    public String toString() {
        return "{" + getWorldObject().getName() +
                ", x: " + getX() +
                ", y: " + getY() +
                ", z: " + getZ() + "}";
    }
}
