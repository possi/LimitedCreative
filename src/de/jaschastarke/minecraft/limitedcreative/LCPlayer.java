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
package de.jaschastarke.minecraft.limitedcreative;

import static de.jaschastarke.minecraft.utils.Locale.L;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;

import de.jaschastarke.minecraft.utils.IPermission;
import de.jaschastarke.minecraft.worldguard.events.PlayerAreaEvent;
import de.jaschastarke.minecraft.worldguard.events.PlayerChangedAreaEvent;

public class LCPlayer {
    private static Core plugin = Core.plugin;
    
    private Player player;
    //private String name;
    private Inventory _inv;
    private GameMode _permanent_gamemode = null;
    //private long _timestamp;
    
    public LCPlayer(Player player) {
        this.player = player;
        //name = player.getName();
        //touch();
        
        if (!this.isActiveRegionGameMode(player.getGameMode())) {
            setInPermanentGameMode(player.getGameMode());
        }
    }
    
    public void updatePlayer(Player player) {
        this.player = player;
    }
    
    public Player getPlayer() {
        return player;
        //return plugin.getServer().getPlayerExact(name); (doesn't work will revive)
    }
    public String getName() {
        return player.getName();
    }
    
    public Inventory getInv() {
        if (_inv == null)
            _inv = new Inventory(this);
        return _inv;
    }
    
    /*public void touch() {
        _timestamp = System.currentTimeMillis();
    }
    public boolean isOutdated() {
        return (getPlayer() == null || !getPlayer().isOnline()) &&
                 _timestamp < (System.currentTimeMillis() - Players.CLEANUP_TIMEOUT);
    }*/

    private Map<String, Object> options = new HashMap<String, Object>();
    public void storeActiveRegionGameMode(final GameMode gm) {
        options.remove("region");
        Core.debug(getName()+": set region game mode: " + gm);
        Players.getOptions().setRegionGameMode(getName(), gm);
    }
    
    public GameMode getActiveRegionGameMode() {
        if (!options.containsKey("region")) {
            options.put("region", Players.getOptions().getRegionGameMode(getName()));
        }
        Core.debug(getName()+": get region game mode: " + options.get("region"));
        return (GameMode) options.get("region");
    }
    public boolean isActiveRegionGameMode(final GameMode gm) {
        return gm.equals(getActiveRegionGameMode());
    }
    public boolean isActiveRegionGameMode() {
        return getActiveRegionGameMode() != null;
    }
    
    public boolean isOptionalRegionGameMode() {
        return getOptionalRegionGameMode() != null;
    }
    public boolean isOptionalRegionGameMode(final GameMode gm) {
        return gm.equals(getOptionalRegionGameMode());
    }
    public boolean isOptionalRegionGameMode(final String region, final GameMode gm) {
        return gm.equals(getOptionalRegionGameMode(region));
    }
    private GameMode getOptionalRegionGameMode() {
        String region = plugin.worldguard.getRegionManager().getRegionsHash(getPlayer().getLocation());
        return getOptionalRegionGameMode(region);
    }
    private GameMode getOptionalRegionGameMode(String region) {
        if (!plugin.config.getRegionRememberOptional())
            return null;
        if (!options.containsKey("region_opt#"+region)) {
            options.put("region_opt#"+region, Players.getOptions().getOptionalRegionGameMode(getName(), region));
        }
        Core.debug(getName()+": get optional region game mode: "+region+" - " + options.get("region_opt#"+region));
        return (GameMode) options.get("region_opt#"+region);
    }
    
    public void setOptionalRegionGameMode(GameMode gm) {
        String region = plugin.worldguard.getRegionManager().getRegionsHash(getPlayer().getLocation());
        setOptionalRegionGameMode(region, gm);
    }
    public void setOptionalRegionGameMode(String region, GameMode gm) {
        if (!plugin.config.getRegionRememberOptional())
            return;
        options.remove("region_opt#"+region);
        Core.debug(getName()+": set optional region game mode: "+region+" - " + gm);
        Players.getOptions().setOptionalRegionGameMode(getName(), region, gm);
    }
    
    public void setInPermanentGameMode(GameMode temp) {
        Core.debug(getName()+": set permanent game mode: " + temp);
        if (temp != null) {
            if (temp.equals(plugin.com.getDefaultGameMode(getPlayer().getWorld()))) {
                temp = null;
            } else {
                storeActiveRegionGameMode(null);
            }
        }
        _permanent_gamemode = temp;
    }
    public boolean isInPermanentGameMode() {
        return isInPermanentGameMode(getPlayer().getGameMode());
    }
    public boolean isInPermanentGameMode(GameMode temp) {
        Core.debug(getName()+": get permanent game mode: " + _permanent_gamemode);
        return temp.equals(_permanent_gamemode);
    }
    
    public boolean onSetGameMode(GameMode gm) {
        Core.debug(getName() + " going into " + gm);
        if (isActiveRegionGameMode()) { // change to the other gamemode as the area defines
            if (!isActiveRegionGameMode(gm)) { // only when we are not switching to the mode the region allows
                if (!plugin.config.getRegionOptional() && (!plugin.config.getPermissionsEnabled() || !hasPermission(Perms.REGIONS_BYPASS))) {
                    getPlayer().sendMessage(ChatColor.RED + L("exception.region.not_optional", gm.toString().toLowerCase()));
                    Core.debug("... denied");
                    return false;
                } else {
                    setOptionalRegionGameMode(gm);
                }
            } else {
                // we are changing to the mode the region defines, thats not permanent
                setOptionalRegionGameMode(null);
                setInPermanentGameMode(null);
            }
        } else {
            setInPermanentGameMode(gm); // we are not in a region, so the mode change is permanent
        }
        
        /*
         * Feature 1: Separated Inventories / Storage
         */
        if (plugin.config.getStoreEnabled()) {
            if (plugin.config.getPermissionToKeepInventory() && hasPermission(Perms.KEEPINVENTORY))
                return true;
            getPlayer().closeInventory();
            
            GameMode cgm = getPlayer().getGameMode();
            if (gm == GameMode.ADVENTURE && !plugin.config.getAdventureInv())
            	gm = GameMode.SURVIVAL;
            if (cgm == GameMode.ADVENTURE && !plugin.config.getAdventureInv())
            	cgm = GameMode.SURVIVAL;
            
            if (gm != cgm) {
	            if (gm != GameMode.CREATIVE || plugin.config.getStoreCreative()) {
	        		getInv().save(cgm);
	            }
	            if (gm == GameMode.CREATIVE) {
	                if (plugin.config.getStoreCreative() && getInv().isStored(GameMode.CREATIVE)) {
	                    getInv().load(GameMode.CREATIVE);
	                } else {
	                    getInv().clear();
	                }
	                setCreativeArmor();
	            } else if (gm == GameMode.SURVIVAL) {
	                if (getInv().isStored(GameMode.SURVIVAL))
	                    getInv().load(GameMode.SURVIVAL);
	            } else if (gm == GameMode.ADVENTURE) {
	                if (getInv().isStored(GameMode.ADVENTURE))
	                	getInv().load(GameMode.ADVENTURE);
	                else
	                	getInv().clear();
	            }
            }
        }
        return true;
    }

    public void onRevive() {
        if (getPlayer().getGameMode() == GameMode.CREATIVE) {
            setCreativeArmor();
        }
    }
    
    public void setCreativeArmor() {
         Map<String, MaterialData> armor = plugin.config.getCreativeArmor();
        if (armor != null) {
            ItemStack[] is = new ItemStack[4];
            if (armor.containsKey("feet"))
                is[0] = armor.get("feet").toItemStack(1);
            if (armor.containsKey("legs"))
                is[1] = armor.get("legs").toItemStack(1);
            if (armor.containsKey("chest"))
                is[2] = armor.get("chest").toItemStack(1);
            if (armor.containsKey("head"))
                is[3] = armor.get("head").toItemStack(1);
            getPlayer().getInventory().setArmorContents(is);
        }
    }
    
    public void onDropItem(PlayerDropItemEvent event) {
        Core.debug(getName() + " ("+getPlayer().getGameMode()+")  drops items " + event.getItemDrop().getItemStack().getType());
        if (getPlayer().getGameMode() == GameMode.CREATIVE) {
            if (plugin.config.getPermissionsEnabled() && hasPermission(Perms.NoLimit.DROP))
                return;
            Core.debug("removed");
            if (plugin.config.getRemoveDrop())
                event.getItemDrop().remove();
            else
                event.setCancelled(true);
        }
    }
    public void onPickupItem(PlayerPickupItemEvent event) {
        if (getPlayer().getGameMode() == GameMode.CREATIVE) {
            if (plugin.config.getPermissionsEnabled() && hasPermission(Perms.NoLimit.PICKUP))
                return;
            if (plugin.config.getBlockPickupInCreative()) {
                event.setCancelled(true);
            } else if(plugin.config.getRemovePickup()) {
                event.getItem().remove();
                event.setCancelled(true);
            }
        }
    }
    
    public void onDie(EntityDeathEvent event) {
        if (getPlayer().getGameMode() == GameMode.CREATIVE) {
            if (!plugin.config.getPermissionsEnabled() || !hasPermission(Perms.NoLimit.DROP)) {
                event.getDrops().clear();
                //getInv().storeTemp();
            }
        }
    }
    /* removed, because much to insecure. also we can save memory with out this
    public void onRespawn(PlayerRespawnEvent event) {
        if (getPlayer().getGameMode() == GameMode.CREATIVE) {
            if (!plugin.config.getPermissionsEnabled() || !hasPermission(Perms.NoLimit.DROP)) {
                getInv().restoreTemp();
            }
        }
        getInv().clearTemp();
    }*/
    
    public void onDamage(Entity from, EntityDamageByEntityEvent event) { // receives damage
        if (from instanceof Player) {
            // its PVP
            Player attacker = (Player) from;
            if (attacker.getGameMode() == GameMode.CREATIVE) {
                if (!plugin.config.getPermissionsEnabled() || !Players.get(attacker).hasPermission(Perms.NoLimit.PVP)) {
                    event.setCancelled(true);
                    return; // skip next check
                }
            }
            if (getPlayer().getGameMode() == GameMode.CREATIVE) {
                if (!plugin.config.getPermissionsEnabled() || !hasPermission(Perms.NoLimit.PVP)) {
                    event.setCancelled(true);
                }
            }
        }
    }
    public void onDealDamage(EntityDamageByEntityEvent event) { // deals damage
        if (event.getEntity() instanceof Creature) {
            if (getPlayer().getGameMode() == GameMode.CREATIVE && plugin.config.getMobDamageBlock()) {
                if (!plugin.config.getPermissionsEnabled() || !hasPermission(Perms.NoLimit.MOB_DAMAGE)) {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    /**
     * don't let the player be target by creatures he can't kill
     */
    public void onTarget(EntityTargetEvent event) {
        if (event.getEntity() instanceof Creature) {
            if (((Player) event.getTarget()).getGameMode() == GameMode.CREATIVE && plugin.config.getMobDamageBlock()) {
                if (!plugin.config.getPermissionsEnabled() || !hasPermission(Perms.NoLimit.MOB_DAMAGE)) {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    public void onChestAccess(PlayerInteractEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
            return;
        if (plugin.config.getPermissionsEnabled() && hasPermission(Perms.NoLimit.CHEST))
            return;
        event.getPlayer().sendMessage(L("blocked.chest"));
        event.setCancelled(true);
    }
    public void onChestAccess(PlayerInteractEntityEvent event) { // chest-minecarts are different events
        if (getPlayer().getGameMode() != GameMode.CREATIVE)
            return;
        if (plugin.config.getPermissionsEnabled() && hasPermission(Perms.NoLimit.CHEST))
            return;
        event.getPlayer().sendMessage(L("blocked.chest"));
        event.setCancelled(true);
    }
    public void onBenchAccess(PlayerInteractEvent event) {
        if (!plugin.config.getBenchBlock() || event.getPlayer().getGameMode() != GameMode.CREATIVE)
            return;
        if (plugin.config.getPermissionsEnabled() && hasPermission(Perms.NoLimit.CHEST))
            return;
        event.getPlayer().sendMessage(L("blocked.chest"));
        event.setCancelled(true);
    }
    public void onSignAccess(PlayerInteractEvent event) {
        if (!plugin.config.getSignBlock() || event.getPlayer().getGameMode() != GameMode.CREATIVE)
            return;
        if (plugin.config.getPermissionsEnabled() && hasPermission(Perms.NoLimit.SIGN))
            return;
        event.getPlayer().sendMessage(L("blocked.sign"));
        event.setCancelled(true);
    }
    public void onButtonAccess(PlayerInteractEvent event) {
        if (!plugin.config.getButtonBlock() || event.getPlayer().getGameMode() != GameMode.CREATIVE)
            return;
        if (event.getClickedBlock().getState() instanceof Lever) {
            if (plugin.config.getPermissionsEnabled() && hasPermission(Perms.NoLimit.LEVER))
                return;
            event.getPlayer().sendMessage(L("blocked.lever"));
            event.setCancelled(true);
        } else {
            if (plugin.config.getPermissionsEnabled() && hasPermission(Perms.NoLimit.BUTTON))
                return;
            event.getPlayer().sendMessage(L("blocked.button"));
            event.setCancelled(true);
        }
    }

    private boolean checkSwitchFlight(PlayerMoveEvent event) {
        if (event != null && getPlayer().getGameMode() == GameMode.CREATIVE && getFloatingHeight(event.getTo()) > plugin.config.getMaximumFloatingHeight()) {
            // but not if he is too high
            this.sendTimeoutMessage(L("blocked.survival_flying"));
            
            Location newloc = event.getTo().clone();
            newloc.setX(event.getFrom().getX());
            newloc.setY(event.getFrom().getY()); // well, otherwise flying high out of the region is possible
            newloc.setZ(event.getFrom().getZ());
            event.setTo(newloc);
            return false;
        }
        return true;
    }
    private boolean checkSwitchFlight(PlayerAreaEvent area_event) {
        if (area_event instanceof PlayerChangedAreaEvent) {
            if (!checkSwitchFlight(((PlayerChangedAreaEvent) area_event).getMoveEvent())) {
                ((PlayerChangedAreaEvent) area_event).setCancelled(true);
                return false;
            }
        }
        return true;
    }
    
    /*
     * Attention: "Creative" stands for "the other gamemode". So true may mean, "be survival in creative world".
     */
    public void setRegionGameMode(GameMode region_gamemode, PlayerAreaEvent area_event) {
        Core.debug(getName()+": changed region: "+region_gamemode+": " + area_event);
        
        PlayerMoveEvent event = null;
        if (area_event instanceof PlayerChangedAreaEvent)
            event = ((PlayerChangedAreaEvent) area_event).getMoveEvent();
        GameMode CURRENT_GAMEMODE = getPlayer().getGameMode();
        GameMode DEFAULT_GAMEMODE = plugin.com.getDefaultGameMode(event != null ? event.getTo().getWorld() : getPlayer().getWorld());
        
        if (region_gamemode != null && CURRENT_GAMEMODE != region_gamemode && !this.isActiveRegionGameMode(region_gamemode)) {
            Core.debug(getName()+": entering creative area");
            // 1. the region allows "the other (temporary) gamemode"
            // 2. but the player is not in that mode
            // 3. and the player is not aware of that
            // result: change him to that mode
            
            boolean isOptional = isOptionalRegionGameMode(area_event.getRegionHash(), CURRENT_GAMEMODE);
            
            if (isOptional || checkSwitchFlight(area_event)) {
                storeActiveRegionGameMode(region_gamemode); // have to be set, before setGameMode
                
                if (!isOptional) {
                    getPlayer().setGameMode(region_gamemode);
                }
            }
        } else if (region_gamemode == null && getPlayer().getGameMode() != DEFAULT_GAMEMODE && !isInPermanentGameMode(CURRENT_GAMEMODE) && getActiveRegionGameMode() != null) {
            Core.debug(getName()+": leaving creative area");
            // 1. the region doesn't allow "the other gamemode"
            // 2. but the player is in that mode
            // 3. and the player isn't global (permanent) in that mode
            // 4. and the player isn't temporary in that mode (that means its maybe a world teleport, and the mode changes afterwards)
            // result: change him back to default mode
            if (checkSwitchFlight(area_event)) {
                storeActiveRegionGameMode(null);
                getPlayer().setGameMode(DEFAULT_GAMEMODE);
            }
        } else if (region_gamemode == null && this.isActiveRegionGameMode()) {
            Core.debug(getName()+": leaving creative area (while already in default gamemode)");
            // 1. the region doesn't allow "the other gamemode"
            // 2. but he thinks he is still allowed
            // 3. (because of else) we are not longer in that mode
            // result: advise him to not longer allowed to that region
            storeActiveRegionGameMode(null);
        }
        /** At the moment, in permanent game mode, it ignores all regions
        else if (this.isRegionGameMode()) {
            Core.debug(getName()+": entering creative area (while already in region gamemode)");
            // 1. the region allow "the other gamemode"
            // 2. (inherit) the player is already in that mode
            GameMode rgm = getOptionalRegionGameMode(area_event.getRegionHash());
            if (rgm != null && rgm != CURRENT_GAMEMODE) {
                Core.debug(getName()+": switching to optional remembered gamemode");
                // 3. but he remembered optional want the other gamemode in that region
                //    * this inherits, that the option is allowed
                // result: change to the optional remembered game mode
                getPlayer().setGameMode(rgm);
            }
        }*/
    }

    private Map<String, Long> timeout_msgs = new HashMap<String, Long>();
    public void sendTimeoutMessage(String msg) {
        Iterator<Map.Entry<String, Long>> i = timeout_msgs.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, Long> entry = i.next();
            if (entry.getValue() < (System.currentTimeMillis() - plugin.config.getRepeatingMessageTimeout())) {
                i.remove();
            }
        }
        if (!timeout_msgs.containsKey(msg)) {
            timeout_msgs.put(msg, System.currentTimeMillis());
            getPlayer().sendMessage(msg);
        }
    }
    
    public int getFloatingHeight() {
        return getFloatingHeight(getPlayer().getLocation());
    }
    public int getFloatingHeight(Location loc) {
        Block b = loc.getBlock();
        int steps = 0;
        while (b.getType() == Material.AIR) {
            steps++;
            b = b.getRelative(BlockFace.DOWN);
        }
        return steps;
    }
    
    public void goToFloor() {
        Block b = getPlayer().getLocation().getBlock();
        int steps = 0;
        while (b.getType() == Material.AIR) {
            steps++;
            b = b.getRelative(BlockFace.DOWN);
        }
        if (steps > 2) {
            getPlayer().teleport(new Location(getPlayer().getWorld(),
                    getPlayer().getLocation().getX(),
                    b.getY()+1,
                    getPlayer().getLocation().getZ()));
        }
    }
    
    public boolean hasPermission(IPermission permission) {
        return plugin.perm.hasPermission(this.getPlayer(), permission);
    }

    public boolean isGameModeAllowed(GameMode gm) {
        if (plugin.config.getRegionOptional() && isActiveRegionGameMode(gm)) {
            return true;
        }
        return false;
    }
}
