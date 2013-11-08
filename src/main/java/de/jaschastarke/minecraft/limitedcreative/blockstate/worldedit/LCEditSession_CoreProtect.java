package de.jaschastarke.minecraft.limitedcreative.blockstate.worldedit;

import net.coreprotect.CoreProtect;
import net.coreprotect.worldedit.CoreProtectEditSession;

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.Region;

import de.jaschastarke.minecraft.limitedcreative.blockstate.DBModel.DBTransaction;

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

    private DBTransaction transaction;

    @Override
    public void flushQueue() {
        transaction = factory.getModel().groupUpdate();
        super.flushQueue();
        if (transaction != null) {
            transaction.finish();
            transaction = null;
        }
    }

    @Override
    public int setBlocks(Region region, BaseBlock block) throws MaxChangedBlocksException {
        boolean useTransaction = false;
        if (transaction == null) {
            transaction = factory.getModel().groupUpdate();
            useTransaction = true;
        }
        int ret;
        try {
            ret = super.setBlocks(region, block);
        } catch (MaxChangedBlocksException e) {
            transaction = null;
            throw e;
        }
        if (transaction != null && useTransaction) {
            transaction.finish();
            transaction = null;
        }
        return ret;
    }

    @Override
    public int setBlocks(Region region, Pattern pattern) throws MaxChangedBlocksException {
        boolean useTransaction = false;
        if (transaction == null) {
            transaction = factory.getModel().groupUpdate();
            useTransaction = true;
        }
        int ret;
        try {
            ret = super.setBlocks(region, pattern);
        } catch (MaxChangedBlocksException e) {
            transaction = null;
            throw e;
        }
        if (transaction != null && useTransaction) {
            transaction.finish();
            transaction = null;
        }
        return ret;
    }

    @Override
    public boolean rawSetBlock(Vector pt, BaseBlock block) {
        boolean success = super.rawSetBlock(pt, block);
        if (success) {
            if (transaction != null) {
                factory.onTransactionBlockEdit(transaction, player, pt, block);
            } else {
                factory.onBlockEdit(player, pt, block);
            }
        }
        return success;
    }
}
