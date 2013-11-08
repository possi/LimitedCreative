package de.jaschastarke.minecraft.limitedcreative.blockstate.worldedit;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import org.bukkit.Location;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;

import de.jaschastarke.minecraft.limitedcreative.ModBlockStates;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockState;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockState.Source;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBModel;
import de.jaschastarke.minecraft.limitedcreative.blockstate.DBModel.DBTransaction;

public class LCEditSessionFactory extends EditSessionFactory {
    private ModBlockStates mod;
    private EditSessionParent parent;
    
    static enum EditSessionParent {
        WORLDEDIT,
        LOGBLOCK("de.jaschastarke.minecraft.limitedcreative.blockstate.worldedit.LCEditSession_LogBlock"),
        COREPROTECT("de.jaschastarke.minecraft.limitedcreative.blockstate.worldedit.LCEditSession_CoreProtect");
        
        private String cls = null;
        EditSessionParent() {
        }
        EditSessionParent(String cls) {
            this.cls = cls;
        }
        public EditSession createInstance(LCEditSessionFactory factory, LocalWorld world, int maxBlocks, LocalPlayer player) {
            if (this.cls != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<EditSession> sessClass = (Class<EditSession>) Class.forName(cls);
                    return sessClass.getConstructor(LCEditSessionFactory.class, LocalWorld.class, int.class, LocalPlayer.class)
                            .newInstance(factory, world, maxBlocks, player);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
            return new LCEditSession(factory, world, maxBlocks, player);
        }
        
        public EditSession createInstance(LCEditSessionFactory factory, LocalWorld world, int maxBlocks, BlockBag blockBag, LocalPlayer player) {
            if (this.cls != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<EditSession> sessClass = (Class<EditSession>) Class.forName(cls);
                    return sessClass.getConstructor(LCEditSessionFactory.class, LocalWorld.class, int.class, LocalPlayer.class)
                            .newInstance(factory, world, maxBlocks, player);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
            return new LCEditSession(factory, world, maxBlocks, player);
        }
    }
    
    public ModBlockStates getLimitedCreativeModule() {
        return mod;
    }

    public LCEditSessionFactory(ModBlockStates mod, EditSessionParent parent) {
        this.mod = mod;
        this.parent = parent;
    }
    
    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks, LocalPlayer player) {
        return parent.createInstance(this, world, maxBlocks, player);
        /*switch (parent) {
            case LOGBLOCK:
                return new LCEditSession_LogBlock(this, world, maxBlocks, player);
            default:
                return new LCEditSession(this, world, maxBlocks, player);
        }*/
    }

    // Without Player, the world isn't know, so we can't opporate
    /*@Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks) {
        return new LCEditSession(this, world, maxBlocks, null);
    }*/
    
    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, LocalPlayer player) {
        return parent.createInstance(this, world, maxBlocks, blockBag, player);
        /*switch (parent) {
            case LOGBLOCK:
                return new LCEditSession_LogBlock(this, world, maxBlocks, blockBag, player);
            default:
                return new LCEditSession(this, world, maxBlocks, blockBag, player);
        }*/
    }
    
    /*@Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag) {
        return new LCEditSession(this, world, maxBlocks, blockBag, null);
    }*/
    
    public static void initFactory(ModBlockStates mod) throws Exception {
        EditSessionFactory currentEditSessionFactory = WorldEdit.getInstance().getEditSessionFactory();
        if (currentEditSessionFactory instanceof LCEditSessionFactory) {
            if (mod.isDebug())
                mod.getLog().debug("WorlEdit-SessionFactory is already hooked");
        } else if (currentEditSessionFactory.getClass().equals(EditSessionFactory.class)) { // not overridden
            if (mod.isDebug())
                mod.getLog().debug("Replacing WorldEdits SessionFactory");
            WorldEdit.getInstance().setEditSessionFactory(new LCEditSessionFactory(mod, EditSessionParent.WORLDEDIT));
        } else if (currentEditSessionFactory.getClass().getName().equals("de.diddiz.worldedit.LogBlockEditSessionFactory")) {
            if (mod.isDebug())
                mod.getLog().debug("Replacing LogBlocks WorldEdit-SessionFactory");
            WorldEdit.getInstance().setEditSessionFactory(new LCEditSessionFactory(mod, EditSessionParent.LOGBLOCK));
        } else if (currentEditSessionFactory.getClass().getName().equals("net.coreprotect.worldedit.CoreProtectEditSessionFactory")) {
            if (mod.isDebug())
                mod.getLog().debug("Replacing CoreProtects WorldEdit-SessionFactory");
            WorldEdit.getInstance().setEditSessionFactory(new LCEditSessionFactory(mod, EditSessionParent.COREPROTECT));
        } else {
            throw new Exception("WorldEdit-SessionFactory is hooked by an unknown another Plugin (" + currentEditSessionFactory.getClass().getName() + ").");
        }
    }
    
    /*public void onBlockEdit(Vector pt, BaseBlock block) {
        this.onBlockEdit(null, pt, block);
    }*/
    
    public DBModel getModel() {
        return mod.getModel();
    }
    
    public boolean onBlockEdit(LocalPlayer player, Vector pt, BaseBlock block) {
        if (player != null) {
            Location loc = new Location(((BukkitWorld) player.getWorld()).getWorld(), pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (block.getType() == 0) {
                mod.getModel().removeState(loc.getBlock());
            } else {
                BlockState s = mod.getModel().getState(loc.getBlock());
                if (s == null) {
                    s = new BlockState();
                    s.setLocation(loc);
                }
                s.setGameMode(null);
                s.setPlayerName(player.getName());
                s.setDate(new Date());
                s.setSource(Source.EDIT);
                if (mod.isDebug())
                    mod.getLog().debug("Saving BlockState: " + s.toString());
                
                mod.getModel().setState(s);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean onTransactionBlockEdit(DBTransaction transaction, LocalPlayer player, Vector pt, BaseBlock block) {
        if (player != null) {
            Location loc = new Location(((BukkitWorld) player.getWorld()).getWorld(), pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (block.getType() == 0) {
                transaction.removeState(loc.getBlock());
            } else {
                BlockState s = mod.getModel().getState(loc.getBlock());
                if (s == null) {
                    s = new BlockState();
                    s.setLocation(loc);
                }
                s.setGameMode(null);
                s.setPlayerName(player.getName());
                s.setDate(new Date());
                s.setSource(Source.EDIT);
                if (mod.isDebug())
                    mod.getLog().debug("Saving BlockState: " + s.toString());
                
                transaction.setState(s);
            }
            return true;
        } else {
            return false;
        }
    }

}
