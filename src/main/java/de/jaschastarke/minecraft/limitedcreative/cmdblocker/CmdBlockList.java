package de.jaschastarke.minecraft.limitedcreative.cmdblocker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.jaschastarke.bukkit.lib.configuration.ConfigurableList;
import de.jaschastarke.bukkit.lib.configuration.IToGeneric;
import de.jaschastarke.bukkit.lib.configuration.command.ITabComplete;
import de.jaschastarke.bukkit.lib.configuration.command.ListConfigValue;
import de.jaschastarke.configuration.InvalidValueException;

public class CmdBlockList extends ArrayList<ICmdBlockEntry> implements ConfigurableList<ICmdBlockEntry>, IToGeneric, ITabComplete {
    private static final long serialVersionUID = -125544131527849084L;

    @Override
    public boolean addSetting(String cmd) throws InvalidValueException {
        if (cmd.startsWith("^")) {
            return add(new RegexpBlockEntry(cmd));
        } else {
            return add(new StringBlockEntry(cmd));
        }
    }

    @Override
    public boolean removeSetting(String e) {
        for (Iterator<ICmdBlockEntry> iterator = this.iterator(); iterator.hasNext();) {
            ICmdBlockEntry entry = iterator.next();
            if (entry.toString().equalsIgnoreCase(e)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> toGeneric() {
        List<String> list = new ArrayList<String>(size());
        for (ICmdBlockEntry bl : this) {
            list.add(bl.toString());
        }
        return list;
    }

    @Override
    public void clearSettings() {
        clear();
    }
    
    public List<String> tabComplete(String[] args, String[] chain) {
        if (args.length > 0 && chain.length > 0) {
            if (chain[chain.length - 1].equalsIgnoreCase(ListConfigValue.REMOVE)) {
                List<String> hints = new ArrayList<String>();
                for (ICmdBlockEntry s : this) {
                    if (s.toString().toLowerCase().startsWith(args[0].toLowerCase()))
                        hints.add(s.toString());
                }
                return hints;
            }
        }
        return null;
    }
}
