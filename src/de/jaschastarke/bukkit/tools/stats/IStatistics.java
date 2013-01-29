package de.jaschastarke.bukkit.tools.stats;

public interface IStatistics {
    public static final String SEPERATOR = "/";
    /**
     * Use the {@see SEPERATOR} to create subgroup of events
     * @param event
     */
    public void trackEvent(String event);
}
