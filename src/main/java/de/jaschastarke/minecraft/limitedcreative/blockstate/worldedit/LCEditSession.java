package de.jaschastarke.minecraft.limitedcreative.blockstate.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;

public class LCEditSession extends EditSession {
    private LCEditSessionFactory factory;
    private LocalPlayer player;

    public LCEditSession(LCEditSessionFactory factory, LocalWorld world, int maxBlocks, BlockBag blockBag, LocalPlayer player) {
        super(world, maxBlocks, blockBag);
        this.factory = factory;
        this.player = player;
    }

    public LCEditSession(LCEditSessionFactory factory, LocalWorld world, int maxBlocks, LocalPlayer player) {
        super(world, maxBlocks);
        this.factory = factory;
        this.player = player;
    }

    @Override
    public boolean rawSetBlock(Vector pt, BaseBlock block) {
        boolean success = super.rawSetBlock(pt, block);
        if (success)
            factory.onBlockEdit(player, pt, block);
        return success;
    }
}
