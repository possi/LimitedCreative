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
package de.jaschastarke.minecraft.limitedcreative.regions.worldguard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.jaschastarke.minecraft.limitedcreative.ModRegions;
import de.jaschastarke.minecraft.limitedcreative.regions.WorldGuardIntegration;
import de.jaschastarke.utils.StringUtil;

public class CustomRegionManager {
    protected YamlConfiguration c;
    protected File file;
    private Map<World, CWorld> worlds = new HashMap<World, CWorld>();
    private ModRegions mod;
    public CustomRegionManager(File file, ModRegions mod) {
        this.file = file;
        this.mod = mod;
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
        public CustomRegionManager getManager() {
            return CustomRegionManager.this;
        }
        private Map<ProtectedRegion, Region> regions = new HashMap<ProtectedRegion, Region>();
        public Region region(ProtectedRegion pr) {
            if (regions.containsKey(pr)) {
                return regions.get(pr);
            } else {
                Region r = new Region(this, pr);
                regions.put(pr, r);
                return r;
            }
        }
        public World getWorld() {
            return world;
        }
        public ProtectedRegion getGlobalRegion() {
            return getWGManager(world).getRegion("__global__");
        }
        
        @SuppressWarnings("unchecked")
        public <V> void storeFlag(Region region, Flag<V> flag, Object value) {
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
            
            if (value == null) {
                fs.set(flag.getName(), null);
            } else {
                fs.set(flag.getName(), flag.marshal((V) value));
            }
            
            try {
                c.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public List<FlagValue> getFlags(Region region) {
            List<FlagValue> list = new ArrayList<FlagValue>();
            
            if (c.contains(world.getName().toLowerCase())) {
                ConfigurationSection wc = c.getConfigurationSection(world.getName().toLowerCase());
                if (wc.contains(region.getProtectedRegion().getId())) {
                    ConfigurationSection rs = wc.getConfigurationSection(region.getProtectedRegion().getId());
                    if (rs.contains("flags")) {
                        ConfigurationSection fs = rs.getConfigurationSection("flags");
                        for (Map.Entry<String, Object> data : fs.getValues(false).entrySet()) {
                            Flag<?> flag = null;
                            if (data.getKey().endsWith("-group")) {
                                flag = FlagList.getFlag(data.getKey().substring(0, data.getKey().length() - 6));
                                if (flag != null)
                                    flag = flag.getRegionGroupFlag();
                            } else {
                                flag = FlagList.getFlag(data.getKey());
                            }
                            if (flag != null) { // the flag doesn't exists anymore. just ignore it without error
                                Object value = flag.unmarshal(data.getValue());
                                list.add(new FlagValue(flag, value));
                            } else {
                                if (mod.isDebug())
                                    mod.getLog().debug("Couldn't load unknown Flag: "+data.getKey());
                            }
                        }
                    }
                }
            }
            return list;
        }
        
        public WorldGuardPlugin getWorldGuard() {
            return CustomRegionManager.this.getWorldGuard();
        }
    }

    private WorldGuardPlugin getWorldGuard() {
        return ((WorldGuardPlugin) mod.getPlugin().getServer().getPluginManager().getPlugin(WorldGuardIntegration.PLUGIN_NAME));
    }

    public RegionManager getWGManager(World world) {
        return getWorldGuard().getRegionManager(world);
    }
    
    public String getRegionsHash(Location loc) {
        StringBuilder hash = new StringBuilder(loc.getWorld().getName());
        List<String> idlist = getWGManager(loc.getWorld()).getApplicableRegionsIDs(BukkitUtil.toVector(loc));
        if (idlist.size() > 0) {
            hash.append("#");
            String[] ids = idlist.toArray(new String[idlist.size()]);
            if (ids.length > 1) {
                Arrays.sort(ids);
            }
            hash.append(StringUtil.join(ids, ","));
        }
        return hash.toString();
    }
    
    public ApplicableRegions getRegionSet(Location loc) {
        return new ApplicableRegions(getWGManager(loc.getWorld()).getApplicableRegions(loc), this.world(loc.getWorld()));
    }

    public ApplicableRegions getRegionSet(Block block) {
        return getRegionSet(block.getLocation());
    }
    
    public boolean isDiffrentRegion(Player player, Location loc) {
        return !getRegionsHash(loc).equals(mod.getPlayerData(player).getHash());
    }
}
