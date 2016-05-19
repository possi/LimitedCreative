package de.jaschastarke.minecraft.limitedcreative.limits;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import de.jaschastarke.bukkit.lib.configuration.ConfigurableList;
import de.jaschastarke.bukkit.lib.configuration.IToGeneric;
import de.jaschastarke.bukkit.lib.configuration.command.ITabComplete;
import de.jaschastarke.bukkit.lib.configuration.command.ListConfigValue;
import de.jaschastarke.bukkit.lib.items.ItemUtils;
import de.jaschastarke.bukkit.lib.items.MaterialDataNotRecognizedException;
import de.jaschastarke.bukkit.lib.items.MaterialNotRecognizedException;
import de.jaschastarke.configuration.InvalidValueException;

public class BlackList extends ArrayList<BlackList.Blacklisted> implements ConfigurableList<BlackList.Blacklisted>, IToGeneric, ITabComplete {
    private static final long serialVersionUID = -3701659163474405152L;

    public static class Blacklisted {
        private String stringRep;
        private MaterialData md;
        private boolean hasData = false;
        
        public Blacklisted(String rep) throws InvalidValueException {
            stringRep = rep;
            try {
                md = ItemUtils.parseMaterial(rep);
                hasData = rep.contains(ItemUtils.MATERIAL_DATA_SEP);
            } catch (MaterialNotRecognizedException e) {
                throw new InvalidValueException("Invalid Material: " + rep, e);
            } catch (MaterialDataNotRecognizedException e) {
                throw new InvalidValueException("Invalid Material with Data: " + rep, e);
            }
        }
        public Blacklisted(Material m) {
            md = new MaterialData(m);
            stringRep = m.toString();
        }
        public Blacklisted(MaterialData md) {
            this.md = md;
            stringRep = md.getItemType().toString() + ItemUtils.MATERIAL_DATA_SEP + Integer.toString(md.getData());
        }
        
        public boolean matches(ItemStack item) {
            if (hasData) {
                return md.equals(item.getData());
            } else {
                return item.getType().equals(md.getItemType());
            }
        }
        public boolean matches(Block block) {
            if (hasData) {
                return md.equals(block.getState().getData());
            } else {
                return block.getType().equals(md.getItemType());
            }
        }
        public String toString() {
            return stringRep;
        }
    }

    
    public BlackList() {
    }
    
    public BlackList(List<?> list) {
        if (list != null) {
            for (Object el : list) {
                if (el instanceof Blacklisted) {
                    add((Blacklisted) el);
                } else {
                    try {
                        if (el != null)
                            addSetting(el.toString());
                    } catch (InvalidValueException e) {
                        System.err.println(e.getCause().getMessage());
                    }
                }
            }
        }
    }

    public boolean contains(String e) {
        for (Blacklisted bl : this) {
            if (bl.toString().equalsIgnoreCase(e))
                return true;
        }
        return false;
    }
    
    public boolean isListed(ItemStack item) {
        for (Blacklisted bl : this) {
            if (bl.matches(item))
                return true;
        }
        return false;
    }
    public boolean isListed(Block block) {
        for (Blacklisted bl : this) {
            if (bl.matches(block))
                return true;
        }
        return false;
    }

    @Override // ConfigurableList, not List<E>
    public boolean addSetting(String e) throws InvalidValueException {
        if (!contains(e)) {
            return add(new Blacklisted(e));
        }
        return false;
    }

    @Override // ConfigurableList, not List<E>
    public boolean removeSetting(String e) {
        Iterator<Blacklisted> it = iterator();
        while (it.hasNext()) {
            if (it.next().toString().equalsIgnoreCase(e)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public List<String> toStringList() {
        List<String> list = new ArrayList<String>(size());
        for (Blacklisted bl : this) {
            list.add(bl.toString());
        }
        return list;
    }

    @Override
    public List<String> toGeneric() {
        return toStringList();
    }

    @Override
    public void clearSettings() {
        clear();
    }

    @Override
    public List<String> tabComplete(String[] args, String[] chain) {
        if (args.length > 0 && chain.length > 0) {
            List<String> hints = new ArrayList<String>();
            String action = chain[chain.length - 1];
            if (action.equalsIgnoreCase(ListConfigValue.ADD)) {
                for (Material m : Material.values()) {
                    if (m.name().toLowerCase().startsWith(args[0].toLowerCase())) {
                        hints.add(m.name());
                    }
                }
            } else if (action.equalsIgnoreCase(ListConfigValue.REMOVE)) {
                for (Blacklisted bl : this) {
                    if (bl.toString().toLowerCase().startsWith(args[0].toLowerCase())) {
                        hints.add(bl.toString());
                    }
                }
            }
            return hints;
        }
        return null;
    }
}
