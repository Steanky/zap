package io.github.zap.zombies.game;

import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.game.arena.ManagedPlayer;
import io.github.zap.arenaapi.util.ItemStackUtils;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.MessageKey;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.DoorData;
import io.github.zap.zombies.game.data.DoorSide;
import io.github.zap.zombies.game.data.MapData;
import io.github.zap.zombies.game.data.WindowData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

public class ZombiesPlayer extends ManagedPlayer<ZombiesPlayer, ZombiesArena> implements Listener {
    @Getter
    private final ZombiesArena arena;

    @Getter
    @Setter
    private ZombiesPlayerState state;

    @Setter
    @Getter
    private int coins;

    @Getter
    @Setter
    private int repairIncrement = 1;

    @Getter
    private boolean inGame = true;

    private WindowData targetWindow;
    private int windowRepairTaskId = -1;
    private int reviveTaskId = -1;

    /**
     * Creates a new ZombiesPlayer instance from the provided values.
     * @param arena The ZombiesArena this player belongs to
     * @param player The underlying Player instance
     * @param coins The number of coins this player starts with
     */
    public ZombiesPlayer(ZombiesArena arena, Player player, int coins) {
        super(arena, player);
        this.arena = arena;
        this.coins = coins;

        arena.getPlayerInteractEvent().registerHandler(this::onPlayerInteract);
        arena.getPlayerToggleSneakEvent().registerHandler(this::onPlayerSneak);
        arena.getPlayerDeathEvent().registerHandler(this::onPlayerDeath);
    }

    private void onPlayerInteract(Event<PlayerInteractEvent> caller, PlayerInteractEvent event) {
        if(event.getPlayer().getUniqueId().equals(getPlayer().getUniqueId())) {
            if(event.getHand() == EquipmentSlot.HAND && state == ZombiesPlayerState.ALIVE) {
                Block block = event.getClickedBlock();

                if(block != null) {
                    Vector clickedVector = block.getLocation().toVector();
                    if(!tryOpenDoor(clickedVector)) {
                        /*
                        if a door wasn't opened, see if there are other right-click actions we can perform gun
                        shooting/inventory item activation/possibly shop activation
                         */
                    }
                }
                else {
                    /*
                    air was clicked, so we also need gun shooting/other inventory item activation code here
                     */
                }
            }
        }
    }

    private void onPlayerSneak(Event<PlayerToggleSneakEvent> caller, PlayerToggleSneakEvent event) {
        if(event.getPlayer().getUniqueId().equals(getPlayer().getUniqueId())) {
            if(event.isSneaking()) {
                if(windowRepairTaskId == -1) {
                    MapData map = arena.getMap();
                    windowRepairTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Zombies.getInstance(),
                            this::checkForWindow, map.getInitialRepairDelay(), map.getWindowRepairTicks());
                }
            }
            else {
                Bukkit.getScheduler().cancelTask(windowRepairTaskId);
                windowRepairTaskId = -1;
            }
        }
    }

    private void onPlayerDeath(Event<PlayerDeathEvent> caller, PlayerDeathEvent event) {
        if(event.getEntity().getUniqueId().equals(getPlayer().getUniqueId())) {
            state = ZombiesPlayerState.KNOCKED;

        /*
        downed player code here. if timeout ends, set state to dead and call arena.checkPlayerState()
        */
        }
    }

    private void addCoins(int amount) {
        Zombies.sendLocalizedMessage(getPlayer(), MessageKey.ADD_GOLD, amount);
        coins += amount;
    }

    private void subtractCoins(int amount) {
        Zombies.sendLocalizedMessage(getPlayer(), MessageKey.SUBTRACT_GOLD, amount);
        coins -= amount;
    }

    public boolean isAlive() {
        return state == ZombiesPlayerState.ALIVE;
    }

    @Override
    public void close() {
        Bukkit.getScheduler().cancelTask(windowRepairTaskId);
    }

    /**
     * Tries to find and repair a window.
     */
    private void checkForWindow() {
        MapData map = arena.getMap();

        if(targetWindow == null) { //our target window is null, so look for one
            WindowData window = map.windowAtRange(getPlayer().getLocation().toVector(), map.getWindowRepairRadius());

            if(window != null) {
                targetWindow = window;
                tryRepairWindow(targetWindow);
            }
        }
        else { //we already have a target window - make sure it's still in range
            if(targetWindow.inRange(getPlayer().getLocation().toVector(), map.getWindowRepairRadius())) {
                tryRepairWindow(targetWindow);
            }
            else {
                targetWindow = null;
            }
        }
    }

    /**
     * Attempts to open the door that may be at the provided vector.
     * @param targetBlock The block to target
     */
    private boolean tryOpenDoor(Vector targetBlock) {
        MapData map = arena.getMap();

        if(ItemStackUtils.isEmpty(getPlayer().getInventory().getItemInMainHand()) || !map.isHandRequiredToOpenDoors()) {
            DoorData door = map.doorAt(targetBlock);

            if(door != null) {
                DoorSide side = door.sideAt(getPlayer().getLocation().toVector());

                if(side != null) {
                    if(coins >= side.getCost()) {
                        WorldUtils.fillBounds(arena.getWorld(), door.getDoorBounds(), map.getDoorFillMaterial());
                        subtractCoins(side.getCost());
                        door.getOpenProperty().set(arena, true);
                        return true;
                    }
                    else { //can't afford door
                        Zombies.sendLocalizedMessage(getPlayer(), MessageKey.CANT_AFFORD);
                    }
                }
            }
        }

        return false;
    }

    /**
     * Attempts to repair the given window.
     */
    private void tryRepairWindow(WindowData targetWindow) {
        Property<Entity> attackingEntityProperty = targetWindow.getAttackingEntityProperty();

        if(attackingEntityProperty.get(arena) == null) {
            Property<ZombiesPlayer> currentRepairerProperty = targetWindow.getRepairingPlayerProperty();
            ZombiesPlayer currentRepairer = currentRepairerProperty.get(arena);

            if(currentRepairer == null) {
                currentRepairer = this;
                currentRepairerProperty.set(arena, this);
            }

            if(currentRepairer == this) {
                //advance repair state
                int previousIndex = targetWindow.getCurrentIndexProperty().get(arena);
                int blocksRepaired = targetWindow.advanceRepairState(arena, repairIncrement);
                if(blocksRepaired > 0) {
                    for(int i = previousIndex; i <= previousIndex + blocksRepaired; i++) { //break the actual blocks
                        WorldUtils.getBlockAt(arena.getWorld(), targetWindow.getFaceVectors().get(i))
                                .setType(targetWindow.getRepairedMaterials().get(i));
                    }

                    addCoins(blocksRepaired * arena.getMap().getCoinsOnRepair());
                }
            }
            else {
                //can't repair because someone else already is, send message to player about that
                Zombies.sendLocalizedMessage(getPlayer(), MessageKey.WINDOW_REPAIR_FAIL_PLAYER);
            }
        }
        else {
            //can't repair because there is a something attacking the window, send message to player about that
            Zombies.sendLocalizedMessage(getPlayer(), MessageKey.WINDOW_REPAIR_FAIL_MOB);
        }
    }
}