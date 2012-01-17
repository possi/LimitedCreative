package de.jaschastarke.minecraft.limitedcreative.serialize;

import org.bukkit.configuration.ConfigurationSection;

public interface Storeable {
    public void store(ConfigurationSection section);
    public void restore(ConfigurationSection section);
}
