package de.jaschastarke.minecraft.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin {
    public final Logger logger = Logger.getLogger("Minecraft");
    
    private List<Module> modules = new ArrayList<Module>();
    
    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> modclass) {
        for (Module module : modules) {
            if (modclass.isInstance(module)) {
                return (T) module;
            }
        }
        return null;
    }
    public Module getModule(String modid) {
        for (Module module : modules) {
            if (module.getIdentifier().equals(modid)) {
                return module;
            }
        }
        return null;
    }

}
