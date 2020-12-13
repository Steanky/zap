package io.github.zap.zombies.game;

import io.github.zap.arenaapi.Property;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
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
        this.arena = arena;
        this.player = player;
        this.coins = coins;

        Zombies zombiesPlugin = Zombies.getInstance();
        zombiesPlugin.getServer().getPluginManager().registerEvents(this, zombiesPlugin);
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getPlayer().getUniqueId().equals(player.getUniqueId()) && isAlive()) {
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

    @EventHandler
    private void onPlayerSneak(PlayerToggleSneakEvent event) {
        if(event.getPlayer().getUniqueId().equals(player.getUniqueId()) && isAlive()) {
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

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        if(event.getEntity().getUniqueId().equals(player.getUniqueId()) && isAlive()) {
            event.setCancelled(true); //we don't want them to respawn normally
            state = ZombiesPlayerState.KNOCKED;

            /*
            downed player code here. if timeout ends, set state to dead and call arena.checkPlayerState()
             */
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        if(event.getPlayer().getUniqueId().equals(player.getUniqueId()) && isInGame()) {
            quit();
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
                        return true;
                    }
                    else { //can't afford door
                        Zombies.sendLocalizedMessage(player, MessageKey.CANT_AFFORD);
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
                Zombies.sendLocalizedMessage(player, MessageKey.WINDOW_REPAIR_FAIL_PLAYER);
            }
        }
        else {
            //can't repair because there is a something attacking the window, send message to player about that
            Zombies.sendLocalizedMessage(player, MessageKey.WINDOW_REPAIR_FAIL_MOB);
        }
    }

    public void addCoins(int amount) {
        Zombies.sendLocalizedMessage(player, MessageKey.ADD_GOLD, amount);
        coins += amount;
    }

    public void subtractCoins(int amount) {
        Zombies.sendLocalizedMessage(player, MessageKey.SUBTRACT_GOLD, amount);
        coins -= amount;
    }

    /**
     * Called when the player rejoins the game
     */
    public void rejoin() {
        inGame = true;

        Zombies zombiesPlugin = Zombies.getInstance();
        zombiesPlugin.getServer().getPluginManager().registerEvents(this, zombiesPlugin);

        /*
        may or may not need to give the player their items and such back
         */
    }

    /**
     * Called when the player leaves the game
     */
    public void quit() {
        state = ZombiesPlayerState.DEAD;
        inGame = false;

        close(); //turn off events and tasks
    }

    /**
     * Unregisters all events and cancels all tasks managed by this player.
     */
    public void close() {
        Bukkit.getScheduler().cancelTask(windowRepairTaskId);
        PlayerInteractEvent.getHandlerList().unregister(this);
        PlayerToggleSneakEvent.getHandlerList().unregister(this);
        PlayerDeathEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }

    public boolean isAlive() {
        return state == ZombiesPlayerState.ALIVE;
    }
}