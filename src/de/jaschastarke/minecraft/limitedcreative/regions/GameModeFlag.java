package de.jaschastarke.minecraft.limitedcreative.regions;

import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;

/**
 * Well, that was an interesting idea, but it doesn't work.
 */
public class GameModeFlag extends Flag<GameModeFlag.State> {
    private State def;
    private RegionGroupFlag groupFlag;
    
    public enum State {
        CREATIVE,
        SURVIVAL,
        NONE;
        
        public GameMode getGameMode() {
            return getBukkitGameMode(this);
        }
        public boolean equals (GameMode gm) {
            return gm == this.getGameMode();
        }
        public static GameMode getBukkitGameMode(State gm) {
            switch (gm) {
                case CREATIVE:
                    return GameMode.CREATIVE;
                case SURVIVAL:
                    return GameMode.SURVIVAL;
                default:
                    return null;
            }
        }
    }
    
    public GameModeFlag(String name, State def) {
        super(name);
        this.def = def;
    }

    public State getDefault() {
        return def;
    }

    public RegionGroupFlag getGroupFlag() {
        return groupFlag;
    }

    public void setGroupFlag(RegionGroupFlag groupFlag) {
        this.groupFlag = groupFlag;
    }
    
    @Override
    public State parseInput(WorldGuardPlugin plugin, CommandSender sender, String input) throws InvalidFlagFormat {
        input = input.trim();
        if (input.equalsIgnoreCase("creative")) {
            return State.CREATIVE;
        } else if (input.equalsIgnoreCase("survival")) {
            return State.SURVIVAL;
        } else if (input.equalsIgnoreCase("none")) {
            return null;
        } else {
            throw new InvalidFlagFormat("Expected none/allow/deny but got '" + input + "'");
        }
    }
    
    @Override
    public State unmarshal(Object o) {
        String input = o.toString();
        if (input.equalsIgnoreCase("creative")) {
            return State.CREATIVE;
        } else if (input.equalsIgnoreCase("survival")) {
            return State.SURVIVAL;
        } else {
            return null;
        }
    }
    
    @Override
    public Object marshal(State o) {
        if (o == State.CREATIVE) {
            return "allow";
        } else if (o == State.SURVIVAL) {
            return "deny";
        } else {
            return null;
        }
    }
}
