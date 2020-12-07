package io.github.zap.zombies.game;

import io.github.zap.arenaapi.PlayerMessageHandler;
import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.util.ItemStackUtils;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.MessageKeys;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.event.map.DoorOpenEvent;
import io.github.zap.zombies.event.player.PlayerJoinArenaEvent;
import io.github.zap.zombies.event.player.PlayerQuitArenaEvent;
import io.github.zap.zombies.event.player.PlayerRepairWindowEvent;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.Vector;

public class ZombiesPlayer implements Listener {
    @Getter
    private final ZombiesArena arena;

    @Getter
    private final Player player;

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
    @Setter
    private boolean isInGame = true;

    private final PluginManager pluginManager;
    private WindowData targetWindow;
    private int windowRepairTaskId = -1;

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

        Zombies zombiesPlugin = Zombies.getInstance();
        pluginManager = zombiesPlugin.getServer().getPluginManager();
        pluginManager.registerEvents(this, zombiesPlugin);
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getPlayer().getUniqueId().equals(player.getUniqueId())) {
            if(event.getHand() == EquipmentSlot.HAND && state == ZombiesPlayerState.ALIVE) {
                Block block = event.getClickedBlock();

                if(block != null) {
                    Vector clickedVector = block.getLocation().toVector();
                    if(!tryOpenDoor(clickedVector)) {
                        //if the door wasn't opened, see if there are other right-click actions we can perform
                        //guns won't shoot if there was a door we clicked, for example
                    }
                }
                else {
                    //clicking air
                }
            }
        }
    }

    @EventHandler
    private void onPlayerSneak(PlayerToggleSneakEvent event) {
        if(event.getPlayer().getUniqueId().equals(player.getUniqueId())) {
            if(event.isSneaking()) {
                if(windowRepairTaskId == -1) {
                    windowRepairTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Zombies.getInstance(),
                            this::checkForWindow, 0, arena.getMap().getWindowRepairTicks());
                }
            }
            else {
                Bukkit.getScheduler().cancelTask(windowRepairTaskId);
                windowRepairTaskId = -1;
            }
        }
    }

    @EventHandler
    private void onPlayerQuitArena(PlayerQuitArenaEvent event) {
        if(event.getAttempt().getPlayers().contains(player.getUniqueId())) {

        }
    }

    @EventHandler
    private void onPlayerJoinArena(PlayerJoinArenaEvent event) {
        if(event.getAttempt().getPlayers().contains(player.getUniqueId())) {

        }
    }


    private void checkForWindow() {
        MapData map = arena.getMap();

        if(targetWindow == null) { //our target window is null, so look for one
            WindowData window = map.windowAtRange(player.getLocation().toVector(), map.getWindowRepairRadius());

            if(window != null) {
                targetWindow = window;
                tryRepairWindow(targetWindow);
            }
        }
        else { //we already have a target window - make sure it's still in range
            if(targetWindow.inRange(player.getLocation().toVector(), map.getWindowRepairRadius())) {
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

        if(ItemStackUtils.isEmpty(player.getInventory().getItemInMainHand()) || !map.isHandRequiredToOpenDoors()) {
            DoorData door = map.doorAt(targetBlock);

            if(door != null) {
                DoorSide side = door.sideAt(player.getLocation().toVector());

                if(side != null) {
                    if(coins >= side.getCost()) {
                        WorldUtils.fillBounds(arena.getWorld(), door.getDoorBounds(), map.getDoorFillMaterial());
                        subtractCoins(side.getCost());
                        door.getOpenProperty().set(arena, true);
                        pluginManager.callEvent(new DoorOpenEvent(this, door, side));
                        return true;
                    }
                    else { //can't afford door
                        PlayerMessageHandler.sendLocalizedMessage(player, MessageKeys.CANT_AFFORD.getKey());
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
                    pluginManager.callEvent(new PlayerRepairWindowEvent(this, targetWindow, blocksRepaired));
                }
            }
            else {
                //can't repair because someone else already is, send message to player about that
                PlayerMessageHandler.sendLocalizedMessage(player, MessageKeys.WINDOW_REPAIR_FAIL_PLAYER.getKey());
            }
        }
        else {
            //can't repair because there is a something attacking the window, send message to player about that
            PlayerMessageHandler.sendLocalizedMessage(player, MessageKeys.WINDOW_REPAIR_FAIL_MOB.getKey());
        }
    }

    public void addCoins(int amount) {
        PlayerMessageHandler.sendLocalizedMessage(player, MessageKeys.ADD_GOLD.getKey(), amount);
        coins += amount;
    }

    public void subtractCoins(int amount) {
        PlayerMessageHandler.sendLocalizedMessage(player, MessageKeys.SUBTRACT_GOLD.getKey(), amount);
        coins -= amount;
    }

    public void close() {
        Bukkit.getScheduler().cancelTask(windowRepairTaskId);
        PlayerInteractEvent.getHandlerList().unregister(this);
        PlayerQuitArenaEvent.getHandlerList().unregister(this);
        PlayerJoinArenaEvent.getHandlerList().unregister(this);
    }
}