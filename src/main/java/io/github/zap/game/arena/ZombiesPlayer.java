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
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;

public class ZombiesPlayer {
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
    }

    /**
     * This is called by the Arena when the player performs a right-click action.
     * @param action The PlayerRightClick event to handle
     */
    public void onRightClickBlock(Action action, Vector vector) {
        if(action == Action.RIGHT_CLICK_BLOCK) {
            tryOpenDoor(vector);
        }
    }

    /**
     * Attempts to open the door that may be at the provided vector.
     * @param targetBlock The block to target
     */
    public void tryOpenDoor(Vector targetBlock) {
        if(state == PlayerState.ALIVE) {
            MapData map = arena.getMap();

            if(ItemStackUtils.isEmpty(player.getInventory().getItemInMainHand()) || !map.isHandRequiredToOpenDoors()) {
                DoorData door = map.doorAt(targetBlock);

                if(door != null) {
                    DoorSide side = door.sideAt(player.getLocation().toVector());

                    if(side != null && coins >= side.getCost()) {
                        WorldUtils.fillBounds(arena.world, door.getDoorBounds(), Material.AIR);
                        coins -= side.getCost();
                        door.getOpenAccessor().set(arena, true);
                        ZombiesPlugin.getInstance().getServer().getPluginManager().callEvent(
                                new DoorOpenEvent(this, door, side));
                    }
                }
            }
        }
    }
    /**
     * Attempts to repair the given window.
     */
    public void tryRepairWindow() {
        if(state == PlayerState.ALIVE) {
            MapData map = arena.getMap();
            WindowData window = map.windowInRange(player.getLocation().toVector(), map.getWindowRepairRadius());

            if(window != null) {
                Property<Entity> attackingEntityAccessor = window.getAttackingEntity();

                if(attackingEntityAccessor.get(arena) == null) {
                    Property<ZombiesPlayer> currentRepairerAccessor = window.getRepairingPlayer();
                    ZombiesPlayer currentRepairer = currentRepairerAccessor.get(arena);

                    if(currentRepairer == null) {
                        currentRepairer = this;
                        currentRepairerAccessor.set(arena, this);
                    }

                    if(currentRepairer == this) {
                        //advance repair state
                        ZombiesPlugin.getInstance().getServer().getPluginManager().callEvent(
                                new PlayerRepairWindowEvent(this, window));
                    }
                    else {
                        //can't repair because someone else already is
                    }
                }
                else {
                    //can't repair because there is a zombie attacking the window
                }
            }
        }
    }
}