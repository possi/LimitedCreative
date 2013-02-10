package de.jaschastarke.minecraft.limitedcreative.cmdblocker;

import de.jaschastarke.bukkit.lib.configuration.Configuration;
import de.jaschastarke.configuration.IConfigurationNode;
import de.jaschastarke.configuration.IConfigurationSubGroup;
import de.jaschastarke.configuration.InvalidValueException;
import de.jaschastarke.configuration.annotations.IsConfigurationNode;
import de.jaschastarke.minecraft.limitedcreative.ModCmdBlocker;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;

public class CmdBlockerConfig extends Configuration implements IConfigurationSubGroup {
    protected ModCmdBlocker mod;
    protected ModuleEntry<IModule> entry;
    
    public CmdBlockerConfig(ModCmdBlocker modCmdBlocker, ModuleEntry<IModule> modEntry) {
        mod = modCmdBlocker;
        entry = modEntry;
    }
    
    @Override
    public void setValue(IConfigurationNode node, Object pValue) throws InvalidValueException {
        super.setValue(node, pValue);
        if (node.getName().equals("enabled")) {
            if (getEnabled()) {
                entry.enable();
            } else {
                entry.disable();
            }
        }
    }

    @Override
    public String getName() {
        return "cmdblock";
    }
    @Override
    public int getOrder() {
        return 500;
    }
    
    /**
     * CmdBlockerEnabled
     * 
     * Enables the feature for blocking certain commands in creative mode.
     * 
     * default: true
     */
    @IsConfigurationNode(order = 100)
    public boolean getEnabled() {
        return config.getBoolean("enabled", true);
    }

    private CmdBlockList blockList;
    
    /**
     * CmdBlockerList
     * 
     * Defines the list of commands that are blocked while in creative mode. The leading / isn't included. By default
     * the list-item is treated as simple string as typed in by the user after the /. All commands starting with
     * this string are blocked, even if more parameteres are entered by the user.
     * If the first character is ^ the entry is interpreted as a regular expression (including the ^ for begin of the string).
     * Only use regular expressions if you know them!
     * 
     * Examples:
     * - home
     * - give diamond
     * - ^home .+
     * - ^chest (one|two|three)
     * - ^(lc|limitedcreative) s(urvival)?\s*$
     * 
     * default: []
     */
    @IsConfigurationNode(order = 200, name = "commands")
    public CmdBlockList getCommandBlockList() {
        if (blockList == null) {
            blockList = new CmdBlockList();
            if (config.contains("commands") && config.isList("commands")) {
                for (Object e : config.getList("commands")) {
                    try {
                        blockList.add(e.toString());
                    } catch (InvalidValueException e1) {
                        mod.getLog().warn(e1.getMessage());
                    }
                }
            }
        }
        return blockList;
    }
}