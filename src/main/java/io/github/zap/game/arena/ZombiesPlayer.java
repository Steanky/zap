package io.github.zap.game.arena;

import io.github.zap.ZombiesPlugin;
import io.github.zap.event.map.DoorOpenEvent;
import io.github.zap.event.player.PlayerRepairWindowEvent;
import io.github.zap.game.Property;
import io.github.zap.game.data.DoorData;
import io.github.zap.game.data.DoorSide;
import io.github.zap.game.data.MapData;
import io.github.zap.game.data.WindowData;
import io.github.zap.util.ItemStackUtils;
import io.github.zap.util.WorldUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.Vector;

public class ZombiesPlayer implements Listener {
    private final PluginManager pluginManager;

    @Getter
    private final ZombiesArena arena;

    @Getter
    private final Player player;

    @Getter
    @Setter
    private PlayerState state;

    @Setter
    @Getter
    private int coins;

    @Getter
    @Setter
    private int repairIncrement = 1;

    private final int windowRepairTaskId;

    /**
     * Creates a new ZombiesPlayer instance from the provided values.
     * @param arena The ZombiesArena this player belongs to
     * @param player The underlying Player instance
     * @param coins The number of coins this player starts with
     */
    public ZombiesPlayer(ZombiesArena arena, Player player, int coins) {
        this.arena = arena;
        this.player = player;
        this.coins = coins;

        ZombiesPlugin zombiesPlugin = ZombiesPlugin.getInstance();
        pluginManager = zombiesPlugin.getServer().getPluginManager();
        pluginManager.registerEvents(this, zombiesPlugin);

        windowRepairTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(zombiesPlugin, this::tryRepairWindow,
                0, arena.getMap().getWindowRepairDelay());
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getPlayer().getUniqueId().equals(player.getUniqueId())) {
            if(event.getHand() == EquipmentSlot.HAND) {
                Block block = event.getClickedBlock();

                if(block != null) {
                    Vector clickedVector = block.getLocation().toVector();
                    if(!tryOpenDoor(clickedVector)) {
                        //if the door wasn't opened, see if there are other right-click actions we can perform
                    }
                }
                else {

                }
            }
        }
    }

    /**
     * Attempts to open the door that may be at the provided vector.
     * @param targetBlock The block to target
     */
    private boolean tryOpenDoor(Vector targetBlock) {
        if(state == PlayerState.ALIVE) {
            MapData map = arena.getMap();

            if(ItemStackUtils.isEmpty(player.getInventory().getItemInMainHand()) || !map.isHandRequiredToOpenDoors()) {
                DoorData door = map.doorAt(targetBlock);

                if(door != null) {
                    DoorSide side = door.sideAt(player.getLocation().toVector());

                    if(side != null) {
                        if(coins >= side.getCost()) {
                            WorldUtils.fillBounds(arena.world, door.getDoorBounds(), map.getDoorFillMaterial());
                            coins -= side.getCost();
                            door.getOpenAccessor().set(arena, true);
                            pluginManager.callEvent(new DoorOpenEvent(this, door, side));
                            return true;
                        }
                        else {
                            //can't afford door
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Attempts to repair the given window.
     */
    private void tryRepairWindow() {
        if(state == PlayerState.ALIVE && player.isSneaking()) {
            MapData map = arena.getMap();
            WindowData targetWindow = map.windowInRange(player.getLocation().toVector(), map.getWindowRepairRadius());

            if(targetWindow != null) {
                Property<Entity> attackingEntityProperty = targetWindow.getAttackingEntity();

                if(attackingEntityProperty.get(arena) == null) {
                    Property<ZombiesPlayer> currentRepairerProperty = targetWindow.getRepairingPlayer();
                    ZombiesPlayer currentRepairer = currentRepairerProperty.get(arena);

                    if(currentRepairer == null) {
                        currentRepairer = this;
                        currentRepairerProperty.set(arena, this);
                    }

                    if(currentRepairer == this) {
                        //advance repair state
                        int previousIndex = targetWindow.getCurrentIndexAccessor().get(arena);
                        int blocksRepaired = targetWindow.advanceRepairState(arena, repairIncrement);
                        if(blocksRepaired > 0) {
                            for(int i = previousIndex; i <= previousIndex + blocksRepaired; i++) { //break the actual blocks
                                WorldUtils.getBlockAt(arena.world, targetWindow.getFaceVectors().get(i))
                                        .setType(targetWindow.getRepairedMaterials().get(i));
                            }

                            pluginManager.callEvent(new PlayerRepairWindowEvent(this, targetWindow, blocksRepaired));
                        }
                    }
                    else {
                        //can't repair because someone else already is, send message to player about that
                    }
                }
                else {
                    //can't repair because there is a zombie attacking the window, send message to player about that
                }
            }
        }
    }

    public void cleanup() {
        Bukkit.getScheduler().cancelTask(windowRepairTaskId);
        PlayerInteractEvent.getHandlerList().unregister(this);
    }
}