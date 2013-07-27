package de.jaschastarke.minecraft.limitedcreative.blockstate.worldedit;

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;

import de.diddiz.LogBlock.LogBlock;
import de.diddiz.worldedit.LogBlockEditSession;

public class LCEditSession_LogBlock extends LogBlockEditSession {
    private LCEditSessionFactory factory;
    private LocalPlayer player;

    public LCEditSession_LogBlock(LCEditSessionFactory factory, LocalWorld world, int maxBlocks, BlockBag blockBag, LocalPlayer player) {
        super(world, maxBlocks, blockBag, player, LogBlock.getInstance());
        this.factory = factory;
        this.player = player;
    }

    public LCEditSession_LogBlock(LCEditSessionFactory factory, LocalWorld world, int maxBlocks, LocalPlayer player) {
        super(world, maxBlocks, player, LogBlock.getInstance());
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
