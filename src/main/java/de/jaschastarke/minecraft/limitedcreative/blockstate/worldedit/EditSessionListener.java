package de.jaschastarke.minecraft.limitedcreative.blockstate.worldedit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.EditSession.Stage;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;

import de.jaschastarke.minecraft.limitedcreative.ModBlockStates;

public class EditSessionListener {
    private ModBlockStates mod;
    
    public EditSessionListener(ModBlockStates mod) {
        this.mod = mod;
    }

    @Subscribe
    public void onEditSession(EditSessionEvent event) {
        if (mod.getConfig().getWorldeditIntegration()) {
            if (event.getStage() == Stage.BEFORE_CHANGE) {
                if (mod.isDebug())
                    mod.getLog().debug("WorldEdit-Integration: New EditSession: "+event.getActor().getName()+" in  World "+event.getWorld().getName()); // + " (" + event.getStage() + ")");
                Actor actor = event.getActor();
                World world = event.getWorld();
                if (actor != null && actor.isPlayer() && world != null && world instanceof BukkitWorld) {
                    Player player = Bukkit.getPlayer(actor.getUniqueId());
                    event.setExtent(new EditSessionExtent(event.getExtent(), mod, player, ((BukkitWorld) event.getWorld()).getWorld()));
                }
            }
        }
    }
    
}
