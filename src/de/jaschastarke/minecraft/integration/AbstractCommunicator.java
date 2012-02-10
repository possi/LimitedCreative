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
package de.jaschastarke.minecraft.integration;

//import java.util.HashMap;
//import java.util.Map;

//import org.bukkit.event.EventHandler;
//import org.bukkit.event.Listener;
/*import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;*/
import org.bukkit.plugin.java.JavaPlugin;

abstract public class AbstractCommunicator /*implements Listener*/ {
    private JavaPlugin plugin;
    //private Map<Class<?>, CommunicationBridge> bridges = new HashMap<Class<?>, CommunicationBridge>();
    
    public AbstractCommunicator(JavaPlugin plugin) {
        this.plugin = plugin;
        //plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    protected boolean isPluginEnabled(String plugin) {
        return this.plugin.getServer().getPluginManager().isPluginEnabled(plugin);
    }
    /*@SuppressWarnings("unchecked")
    protected <T extends CommunicationBridge> T getBridge(Class<T> cls) {
        if (!bridges.containsKey(cls)) {
            try {
                bridges.put(cls, cls.newInstance());
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return (T) bridges.get(cls);
    }*/
    
    /*@EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        
    }
    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        
    }
    protected <T extends CommunicationBridge> boolean isPluginEnabled(String plugin, Class<T> cls) {
        if (!bridges.containsKey(plugin)) {
            if (this.plugin.getServer().getPluginManager().isPluginEnabled(plugin)) {
                
            } else {
                bridges.put(plugin, null);
            }
        }
        return bridges.get(plugin) != null;
    }
    protected 
    protected <T extends CommunicationBridge> T whenPluginEnabled(String plugin, Class<T> cls) {
        if (isPluginEnabled(plugin)) {
            
        }
    }*/
}
