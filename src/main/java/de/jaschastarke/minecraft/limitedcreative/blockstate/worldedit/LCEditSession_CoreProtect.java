package de.jaschastarke.minecraft.limitedcreative.blockstate.worldedit;

import net.coreprotect.CoreProtect;
import net.coreprotect.worldedit.CoreProtectEditSession;

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;

public class LCEditSession_CoreProtect extends CoreProtectEditSession {
    private LCEditSessionFactory factory;
    private LocalPlayer player;

    public LCEditSession_CoreProtect(LCEditSessionFactory factory, LocalWorld world, int maxBlocks, BlockBag blockBag, LocalPlayer player) {
        super(world, maxBlocks, blockBag, player, CoreProtect.getInstance());
        this.factory = factory;
        this.player = player;
    }

    public LCEditSession_CoreProtect(LCEditSessionFactory factory, LocalWorld world, int maxBlocks, LocalPlayer player) {
        super(world, maxBlocks, player, CoreProtect.getInstance());
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
