package de.jaschastarke.minecraft.limitedcreative.limits;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import de.jaschastarke.bukkit.lib.configuration.Configuration;
import de.jaschastarke.bukkit.lib.configuration.IToGeneric;
import de.jaschastarke.configuration.IConfigurationNode;
import de.jaschastarke.configuration.IConfigurationSubGroup;
import de.jaschastarke.configuration.InvalidValueException;
import de.jaschastarke.configuration.annotations.IsConfigurationNode;
import de.jaschastarke.maven.ArchiveDocComments;
import de.jaschastarke.minecraft.limitedcreative.ModCreativeLimits;
import de.jaschastarke.modularize.IModule;
import de.jaschastarke.modularize.ModuleEntry;
import de.jaschastarke.modularize.ModuleEntry.ModuleState;

/**
 * Creative Limits-Feature
 * 
 * http://dev.bukkit.org/server-mods/limited-creative/pages/features/limit/
 */
@ArchiveDocComments
public class LimitConfig extends Configuration implements IConfigurationSubGroup {

    protected ModCreativeLimits mod;
    protected ModuleEntry<IModule> entry;
    
    public LimitConfig(ModCreativeLimits modInventories, ModuleEntry<IModule> modEntry) {
        mod = modInventories;
        entry = modEntry;
    }
    
    @Override
    public void setValue(IConfigurationNode node, Object pValue) throws InvalidValueException {
        if (!(pValue instanceof BlackList))
            super.setValue(node, pValue);
        if (node.getName().equals("enabled")) {
            if (getEnabled()) {
                if (entry.initialState != ModuleState.NOT_INITIALIZED)
                    entry.enable();
            } else {
                entry.disable();
            }
        }
    }
    
    @Override
    public void setValues(ConfigurationSection sect) {
        super.setValues(sect);
        if (entry.initialState != ModuleState.NOT_INITIALIZED)
            entry.initialState = getEnabled() ? ModuleState.ENABLED : ModuleState.DISABLED;
        
        // Config Upgrade
        if (!sect.contains("interact") && sect.contains("sign")) {
            interactList = new BlackList();
            if (config.getBoolean("sign", true)) {
                interactList.add(new BlackList.Blacklisted(Material.WALL_SIGN));
                interactList.add(new BlackList.Blacklisted(Material.SIGN_POST));
            }
            if (config.getBoolean("button", false)) {
                interactList.add(new BlackList.Blacklisted(Material.LEVER));
                interactList.add(new BlackList.Blacklisted(Material.STONE_BUTTON));
                interactList.add(new BlackList.Blacklisted(Material.WOOD_BUTTON));
            }
            if (config.getBoolean("workbench", false)) {
                interactList.add(new BlackList.Blacklisted(Material.WORKBENCH));
                interactList.add(new BlackList.Blacklisted(Material.ANVIL));
                interactList.add(new BlackList.Blacklisted(Material.ENCHANTMENT_TABLE));
            }
        }
        if (!sect.contains("removeDrops") && sect.contains("remove_drops"))
            sect.set("removeDrops", sect.getBoolean("remove_drops"));
        if (!sect.contains("damageToMobs") && sect.contains("damagemob"))
            sect.set("damageToMobs", sect.getBoolean("damagemob"));
    }
    @Override
    public String getName() {
        return "limit";
    }
    @Override
    public int getOrder() {
        return 200;
    }
    
    /**
     * LimitEnabled
     * 
     * Prevents all players in creative-mode from:
     *  - accessing chests
     *  - dropping items from "inventory" to the ground
     *  - doing PvP (wouldn't be fair, would it?)
     * 
     * Also if this option is disabled all other Limit-Options below are disabled too. To just disable some of these 
     * limitations, use the "nolimit"-permissions.
     * 
     * default: true
     */
    @IsConfigurationNode(order = 100)
    public boolean getEnabled() {
        return config.getBoolean("enabled", true);
    }
    
    /**
     * LimitDropsInsteadPrevent
     * 
     * When enabled items that are dropped by creative players are removed (burning in the hellfire or so, they just 
     * disappear). When disabled the items stay in the inventory of the player.
     * 
     * default: false
     */
    @IsConfigurationNode(order = 200)
    public boolean getRemoveDrops() {
        return config.getBoolean("removeDrops", false);
    }
    
    
    public static enum BlockPickup implements IToGeneric {
        PREVENT,
        REMOVE;
        
        @Override
        public Object toGeneric() {
            return name().toLowerCase();
        }
    }
    
    /**
     * LimitDamageToMobs
     * 
     * Prevents dealing damage to all creatures when the player is in creative (friendly sheeps as well as hostile 
     * creepers).
     * 
     * default: false
     */
    @IsConfigurationNode(name = "damageToMobs", order = 300)
    public boolean getBlockDamageMob() {
        return config.getBoolean("damageToMobs", false);
    }
    
    
    /**
     * LimitPickup
     * 
     * Prevents the pickup of items while in creative mode. Either the items are just stay on ground and ignore that a 
     * creative player walks over it ("prevent"), or the are "remove"d when a creative player walks over it. This is 
     * helpful e.g. when the creative player destroys a long line of rails.
     * 
     * valid options: remove / prevent / false
     * default: remove
     */
    @IsConfigurationNode(name = "pickup", order = 300)
    public BlockPickup getBlockPickup() {
        if (config.contains("pickup") && config.isBoolean("pickup") && config.getBoolean("pickup")) {
            return !config.contains("remove_pickup") || config.getBoolean("remove_pickup")
                        ? BlockPickup.REMOVE
                        : BlockPickup.PREVENT;
        }
        return getEnum(BlockPickup.class, "pickup", BlockPickup.PREVENT);
    }
    
    
    private BlackList interactList;
    /**
     * LimitInteraction
     * 
     * Prevents players of interacting with specific blocks as addition to chests in creative mode (and only in 
     * creative).
     * 
     * You can use the technical name (http://tinyurl.com/bukkit-material) or 
     * the id of the block/item (better use the id, if you're not sure). You may add the data separated with a colon 
     * e.g.: "WOOL:11" blocks blue wool. But be sure to put it in quotes, to not break yml-configuration! Named data 
     * values aren't supported yet. If you don't add a data-value, all blocks of this material are blocked.
     * 
     * default:
     *  - SIGN
     *  - SIGN_POST
     *  - LEVER
     *  - STONE_BUTTON
     *  - WOOD_BUTTON
     */
    @IsConfigurationNode(name = "interact", order = 600)
    public BlackList getBlockInteraction() {
        if (interactList == null) {
            interactList = new BlackList(config.getList("interact"));
            if (!config.contains("interact")) {
                interactList.add(new BlackList.Blacklisted(Material.WALL_SIGN));
                interactList.add(new BlackList.Blacklisted(Material.SIGN_POST));
                interactList.add(new BlackList.Blacklisted(Material.LEVER));
                interactList.add(new BlackList.Blacklisted(Material.STONE_BUTTON));
                interactList.add(new BlackList.Blacklisted(Material.WOOD_BUTTON));
            }
        }
        return interactList;
    }
    private BlackListEntity interactEntityList;
    /**
     * LimitEntityInteraction
     * 
     * Prevents players of interacting with specific entities in creative mode (and only in creative).
     * 
     * You can use the technical name (see http://tinyurl.com/bukkit-entity) or the id of the entity (better use the id, 
     * if you're not sure). 
     * 
     * default:
     *  - ITEM_FRAME
     */
    @IsConfigurationNode(name = "entityInteract", order = 650)
    public BlackListEntity getBlockEntityInteraction() {
        if (interactEntityList == null) {
            interactEntityList = new BlackListEntity(config.getList("entityInteract"));
            if (!config.contains("entityInteract")) {
                interactEntityList.add(new BlackListEntity.Blacklisted(EntityType.VILLAGER));
            }
        }
        return interactEntityList;
    }
    
    private BlackList useList;
    /**
     * LimitUse
     * 
     * Prevents players of using or placing specific items/blocks in creative mode (and only in creative).
     * 
     * You can use the technical name (see http://tinyurl.com/bukkit-material) or 
     * the id of the block/item (better use the id, if you're not sure). You may add the data separated with a colon 
     * e.g.: "WOOL:11" blocks blue wool. But be sure to put it in quotes, to not break yml-configuration! Named data 
     * values aren't supported yet. If you don't add a data-value, all blocks of this material are blocked.
     * 
     * default:
     *  - EXP_BOTTLE
     *  - BEDROCK
     */
    @IsConfigurationNode(name = "use", order = 700)
    public BlackList getBlockUse() {
        if (useList == null) {
            useList = new BlackList(config.getList("use"));
            if (!config.contains("use")) {
                useList.add(new BlackList.Blacklisted(Material.EXP_BOTTLE));
                useList.add(new BlackList.Blacklisted(Material.BEDROCK));
            }
        }
        return useList;
    }
    
    private BlackList breakList;
    /**
     * LimitBreak
     * 
     * Prevents players of destroying specific blocks in creative mode (and only in creative).
     * 
     * You can use the technical name (see http://tinyurl.com/bukkit-material) or 
     * the id of the block/item (better use the id, if you're not sure). You may add the data separated with a colon 
     * e.g.: "WOOL:11" blocks blue wool. But be sure to put it in quotes, to not break yml-configuration! Named data 
     * values aren't supported yet. If you don't add a data-value, all blocks of this material are blocked.
     * 
     * default:
     *  - BEDROCK
     */
    @IsConfigurationNode(name = "break", order = 800)
    public BlackList getBlockBreak() {
        if (breakList == null) {
            breakList = new BlackList(config.getList("use"));
            if (!config.contains("break")) {
                breakList.add(new BlackList.Blacklisted(Material.BEDROCK));
            }
        }
        return breakList;
    }
    

    @Override
    public Object getValue(final IConfigurationNode node) {
        Object val = super.getValue(node);
        if (node.getName().equals("pickup") && val == null) {
            return new Boolean(false);
        } else {
            return val;
        }
    }
}
