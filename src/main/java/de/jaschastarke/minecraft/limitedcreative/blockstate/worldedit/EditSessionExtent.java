package de.jaschastarke.minecraft.limitedcreative.blockstate.worldedit;

import java.util.Date;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.logging.AbstractLoggingExtent;

import de.jaschastarke.minecraft.limitedcreative.ModBlockStates;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockState;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockState.Source;

public class EditSessionExtent extends AbstractLoggingExtent {
    private ModBlockStates mod;
    private Player player = null;
    private World world;
    
    public EditSessionExtent(Extent extent, ModBlockStates mod, Player player, World world) {
        super(extent);
        this.mod = mod;
        this.player = player;
        this.world = world;
    }

    /**
     * Called when a block is being changed.
     *
     * @param position the position
     * @param newBlock the new block to replace the old one
     */
    protected void onBlockChange(Vector pt, BaseBlock newBlock) {
        if (mod.isDebug())
            mod.getLog().debug("WorldEdit-Integration: BlockChange: "+pt.toString()+" BB: " + newBlock.toString());
        Location loc = new Location(world, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (newBlock.getType() == 0) {
            mod.getModel().removeState(loc.getBlock());
        } else {
            BlockState s = mod.getModel().getState(loc.getBlock());
            if (s == null) {
                s = new BlockState();
                s.setLocation(loc);
            }
            s.setGameMode(null);
            s.setPlayerName(player.getUniqueId().toString());
            s.setDate(new Date());
            s.setSource(Source.EDIT);
            if (mod.isDebug())
                mod.getLog().debug("WorldEdit-Integration: Saving BlockState: " + s.toString());
            
            mod.getModel().setState(s);
        }
    }
}
