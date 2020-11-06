package io.github.zap.game.arena;

import io.github.zap.ZombiesPlugin;
import io.github.zap.event.map.DoorOpenEvent;
import io.github.zap.event.player.PlayerRightClickEvent;
import io.github.zap.game.data.DoorData;
import io.github.zap.game.data.DoorSide;
import io.github.zap.game.data.MapData;
import io.github.zap.util.ItemStackUtils;
import io.github.zap.util.WorldUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
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

    private int lastTps = -1;
    private int repairTickOn;
    private int repairTick = 0;

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

        calculateTimings();
    }

    /**
     * If the speed of the Ticker is changed midgame, we must adjust our timings to ensure window repairs and
     * reviving progress at the same speed
     */
    private void calculateTimings() {
        int currentTps = ZombiesPlugin.getInstance().getTicker().getTps();

        if(currentTps != lastTps) {
            int adjustFactor = (20 / currentTps);
            repairTickOn = arena.getMap().getWindowRepairDelay() * adjustFactor;
            lastTps = currentTps;
        }
    }

    /**
     * This is called periodically by the plugin's Ticker.
     */
    public void onPlayerTick() {
        if(repairTick++ == repairTickOn) {
            if(state == PlayerState.ALIVE && player.isSneaking()) {
                arena.tryRepairWindow(this);
            }

            calculateTimings();
        }
    }

    /**
     * This is called by the Arena when the player performs a right-click action.
     * @param event The PlayerRightClick event to handle
     */
    public void playerRightClick(PlayerRightClickEvent event) {
        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            arena.tryOpenDoor(this, event.getClicked().getLocation().toVector());
        }
    }

    /**
     * Gives the player a certain amount of coins. Give a negative number to remove coins.
     * @param amount The amount of coins to give
     */
    public void giveCoins(int amount) {
        coins += amount;
    }

    /**
     * Attempts to open the door that may be at the provided vector, given the provided player.
     * @param opener The player that's trying to open the door
     * @param targetBlock The block to target
     */
    public void tryOpenDoor(ZombiesPlayer opener, Vector targetBlock) {
        if(opener.getState() == PlayerState.ALIVE) {
            Player player = opener.getPlayer();
            MapData map = arena.getMap();

            if(ItemStackUtils.isEmpty(player.getInventory().getItemInMainHand()) || !map.isHandRequiredToOpenDoors()) {
                DoorData door = map.doorAt(targetBlock);

                if(door != null) {
                    DoorSide side = door.sideAt(player.getLocation().toVector());

                    if(side != null && opener.getCoins() >= side.getCost()) {
                        WorldUtils.fillBounds(arena.world, door.getDoorBounds(), Material.AIR);
                        opener.giveCoins(-side.getCost());
                        door.getOpenAccessor().set(arena, true);
                        ZombiesPlugin.getInstance().getServer().getPluginManager().callEvent(new DoorOpenEvent(opener,
                                door, side));
                    }
                }
            }
        }
    }
}
