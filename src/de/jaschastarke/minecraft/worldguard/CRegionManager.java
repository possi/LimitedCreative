/*
 * Limited Creative - (Bukkit Plugin)
 * Copyright (C) 2012 jascha@ja-s.de
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.jaschastarke.minecraft.worldguard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class CRegionManager {
    protected YamlConfiguration c;
    protected File file;
    private Map<World, CWorld> worlds = new HashMap<World, CWorld>();
    public CRegionManager(File f) {
        file = f;
        c = YamlConfiguration.loadConfiguration(file);
    }
    
    
    public CWorld world(World w) {
        if (worlds.containsKey(w)) {
            return worlds.get(w);
        } else {
            CWorld r = new CWorld(w);
            worlds.put(w, r);
            return r;
        }
    }
    
    public class CWorld {
        private World world;
        private ConfigurationSection wc = null;
        public CWorld(World w) {
            world = w;
        }
        public CRegionManager getManager() {
            return CRegionManager.this;
        }
        private Map<ProtectedRegion, CRegion> regions = new HashMap<ProtectedRegion, CRegion>();
        public CRegion region(ProtectedRegion pr) {
            if (regions.containsKey(pr)) {
                return regions.get(pr);
            } else {
                CRegion r = new CRegion(this, pr);
                regions.put(pr, r);
                return r;
            }
        }
        public World getWorld() {
            return world;
        }
        @SuppressWarnings("unchecked")
        public <V> void storeFlag(CRegion region, Flag<V> flag, Object value) {
            if (wc == null) {
                if (c.contains(world.getName().toLowerCase()))
                    wc = c.getConfigurationSection(world.getName().toLowerCase());
                else
                    wc = c.createSection(world.getName().toLowerCase());
            }
            
            ConfigurationSection rs;
            if (wc.contains(region.getProtectedRegion().getId()))
                rs = wc.getConfigurationSection(region.getProtectedRegion().getId());
            else
                rs = wc.createSection(region.getProtectedRegion().getId());

            ConfigurationSection fs = rs.contains("flags") ? rs.getConfigurationSection("flags") : rs.createSection("flags");
            
            fs.set(flag.getName(), flag.marshal((V) value));
            
            try {
                c.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public List<FlagValue> getFlags(CRegion region) {
            List<FlagValue> list = new ArrayList<FlagValue>();
            
            if (c.contains(world.getName().toLowerCase())) {
                ConfigurationSection wc = c.getConfigurationSection(world.getName().toLowerCase());
                if (wc.contains(region.getProtectedRegion().getId())) {
                    ConfigurationSection rs = wc.getConfigurationSection(region.getProtectedRegion().getId());
                    if (rs.contains("flags")) {
                        ConfigurationSection fs = rs.getConfigurationSection("flags");
                        for (Map.Entry<String, Object> data : fs.getValues(false).entrySet()) {
                            Flag<?> flag = FlagList.getFlag(data.getKey());
                            Object value = flag.unmarshal(data.getValue());
                            list.add(new FlagValue(flag, value));
                        }
                    }
                }
            }
            return list;
        }
    }
}
