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
package de.jaschastarke.minecraft.limitedcreative;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import static de.jaschastarke.minecraft.utils.Util.copyFile;
import static de.jaschastarke.minecraft.utils.Locale.L;

public class Configuration {
    private FileConfiguration c;
    private File file;
    public static LimitedCreativeCore plugin;
    
    public enum Option {
        STORECREATIVE("store.creative", true),
        REGION_OPTIONAL("region.optional", true),
        BLOCKPICKUP("limit.pickup", true),
        BLOCKSIGN("limit.sign", true),
        BLOCKBUTTON("limit.button", false),
        BLOCKDAMAGEMOB("limit.damagemob", false),
        BLOCKBENCHES("limit.workbench", false),
        REMOVEDROP("limit.remove_drops", true),
        REMOVEPICKUP("limit.remove_pickup", false),
        PERMISSIONS("permissions.enabled", false),
        PERM_KEEPINVENTORY("permissions.keepinventory", false),
        DEBUG("debug", false);
        
        private String key;
        private boolean _default;
        private Option(String key, boolean def) {
            this.key = key;
            this._default = def;
        }
        public String getKey() {
            return key;
        }
        public boolean getDefault() {
            return _default;
        }
        public static List<Option> getAvailableOptions() {
            List<Option> ret = new ArrayList<Option>(Arrays.asList(Option.values()));
            ret.remove(Option.DEBUG); // keep it undocumented ;)
            return ret;
        }
    }
    
    public Configuration(LimitedCreativeCore pplugin) {
        plugin = pplugin;
        
        file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists())
            //plugin.saveDefaultConfig();
            copyFile(plugin.getResource("config.yml"), file);
        
        c = plugin.getConfig();
    }
    
    public void set(Option opt, boolean value) {
        /*if (!opt.isSetAble())
            throw new IllegalArgumentException("Setting this option is not allowed");*/
        this.reload();
        c.set(opt.getKey(), value);
        if (value && opt == Option.PERM_KEEPINVENTORY && !this.getPermissionsEnabled())
            c.set(Option.PERMISSIONS.getKey(), true);
        this.save();
    }
    
    public boolean getBoolean(Option opt) {
        return c.getBoolean(opt.getKey(), opt.getDefault());
    }

    /**
     * Intended to be undocumented ;)
     */
    public boolean getDebug() {
        return this.getBoolean(Option.DEBUG);
    }
    
    public boolean getStoreEnabled() {
        return getTempStoreEnabled() && c.getBoolean("store.enabled", true);
    }
    public boolean getLimitEnabled() {
        return c.getBoolean("limit.enabled", true);
    }
    public boolean getRegionEnabled() {
        return c.getBoolean("region.enabled", true);
    }
    
    public boolean getStoreCreative() {
        return this.getBoolean(Option.STORECREATIVE);
    }
    public boolean getUnsafeStorage() {
        return c.getBoolean("store.unsafe", false);
    }
    public String getInventoryFolder() {
        return c.getString("store.folder", "inventories");
    }
    public boolean getBlockPickupInCreative() {
        return this.getBoolean(Option.BLOCKPICKUP);
    }
    public boolean getSignBlock() {
        return this.getBoolean(Option.BLOCKSIGN);
    }
    public boolean getBenchBlock() {
        return this.getBoolean(Option.BLOCKBENCHES);
    }
    public boolean getButtonBlock() {
        return this.getBoolean(Option.BLOCKBUTTON);
    }
    public boolean getRemoveDrop() {
        return this.getBoolean(Option.REMOVEDROP);
    }
    public boolean getRemovePickup() {
        return this.getBoolean(Option.REMOVEPICKUP);
    }
    public boolean getMobDamageBlock() {
        return this.getBoolean(Option.BLOCKDAMAGEMOB);
    }
    
    public boolean getPermissionsEnabled() {
        return this.getBoolean(Option.PERMISSIONS);
    }
    public boolean getPermissionToKeepInventory() {
        return this.getPermissionsEnabled() && this.getBoolean(Option.PERM_KEEPINVENTORY);
    }
    public boolean getRegionOptional() {
        return this.getBoolean(Option.REGION_OPTIONAL);
    }
    
    public String getLocale() {
        if (c.contains("locale") && c.getString("locale") != "none")
            return c.getString("locale");
        return null;
    }

    protected void reload() {
        _block_break = null;
        _block_use = null;
        plugin.reloadConfig();
        c = plugin.getConfig();
    }
    protected void save() {
        plugin.saveConfig();
    }
    
    private List<BlackList> _block_break = null;
    private List<BlackList> _block_use = null;
    
    public List<BlackList> getBlockedBreaks() {
        if (_block_break == null)
            _block_break = parseMaterialList(c.getStringList("limit.break"));
        return _block_break;
    }
    
    public List<BlackList> getBlockedUse() {
        if (_block_use == null)
            _block_use = parseMaterialList(c.getStringList("limit.use"));
        return _block_use;
    }
    
    private List<BlackList> parseMaterialList(List<String> s) {
        List<BlackList> list = new ArrayList<BlackList>();
        if (s != null) {
            for (String m : s) {
                int d = -1;
                if (m.equals("*")) {
                    list.clear();
                    list.add(new BlackList.All());
                    break;
                } else {
                    if (m.contains(":")) {
                        String[] t = m.split(":");
                        m = t[0];
                        try {
                            d = Integer.parseInt(t[1]);
                        } catch (NumberFormatException ex) {
                            // TODO: try to find the data value by 
                            if (d == -1)
                                plugin.warn(L("exception.config.materiak_data_not_found", t[1]));
                        }
                    }
                    Material e = null;
                    try {
                        e = Material.getMaterial(Integer.parseInt(m));
                    } catch (NumberFormatException ex) {
                        e = Material.matchMaterial(m);
                    }
                    if (e == null) {
                        plugin.warn(L("exception.config.material_not_found", m));
                    } else {
                        list.add(new BlackList.Some(e, d));
                    }
                }
            }
        }
        return list;
    }
    
    private boolean _store_enabled = true;
    public void setTempStoreEnabled(boolean b) {
        _store_enabled = b;
    }
    public boolean getTempStoreEnabled() {
        return _store_enabled;
    }
}
