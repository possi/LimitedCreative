package de.jaschastarke.minecraft.limitedcreative.limits;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.jaschastarke.bukkit.lib.configuration.command.ITabComplete;
import de.jaschastarke.bukkit.lib.configuration.command.ListConfigValue;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import de.jaschastarke.bukkit.lib.configuration.ConfigurableList;
import de.jaschastarke.bukkit.lib.configuration.IToGeneric;
import de.jaschastarke.configuration.InvalidValueException;

public class BlackListEntity extends ArrayList<BlackListEntity.Blacklisted> implements ConfigurableList<BlackListEntity.Blacklisted>, IToGeneric, ITabComplete {
    private static final long serialVersionUID = 6150727863411513873L;

    @Override
    public List<String> tabComplete(String[] args, String[] chain) {
        if (args.length > 0 && chain.length > 0) {
            List<String> hints = new ArrayList<String>();
            String action = chain[chain.length - 1];
            if (action.equalsIgnoreCase(ListConfigValue.ADD)) {
                for (EntityType m : EntityType.values()) {
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

    public static class Blacklisted {
        private String stringRep;
        private EntityType type;
        
        public Blacklisted(String rep) throws InvalidValueException {
            try {
                int val = Integer.parseInt(rep);
                if (val > 0)
                    type = EntityType.fromId(val);
            } catch (NumberFormatException e) {
                type = null;
            }
            if (type == null)
                type = EntityType.valueOf(rep);
            try {
                if (type == null)
                    type = EntityType.valueOf(rep);
            } catch (IllegalArgumentException e) {
                type = null;
            }
            
            if (type == null)
                throw new InvalidValueException("Entity '" + rep + "' not found");
            stringRep = rep;
        }
        public Blacklisted(EntityType et) {
            type = et;
            stringRep = et.toString();
        }
        
        public boolean matches(Entity entity) {
            return matches(entity.getType());
        }
        public boolean matches(EntityType et) {
            return type.equals(et);
        }
        public String toString() {
            return stringRep;
        }
    }

    
    public BlackListEntity() {
    }
    
    public BlackListEntity(List<?> list) {
        if (list != null) {
            for (Object el : list) {
                if (el instanceof Blacklisted) {
                    add((Blacklisted) el);
                } else {
                    try {
                        if (el != null)
                            addSetting(el.toString());
                    } catch (InvalidValueException e) {
                        System.err.println((e.getCause() != null ? e.getCause() : e).getMessage());
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
    
    public boolean isListed(Entity entity) {
        for (Blacklisted bl : this) {
            if (bl.matches(entity))
                return true;
        }
        return false;
    }
    public boolean isListed(EntityType et) {
        for (Blacklisted bl : this) {
            if (bl.matches(et))
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
}
