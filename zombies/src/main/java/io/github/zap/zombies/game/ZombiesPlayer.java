package io.github.zap.zombies.game;

import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.game.arena.ManagedPlayer;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.MessageKey;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.equipment.EquipmentManager;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.WindowData;
import io.github.zap.zombies.game.hotbar.ZombiesHotbarManager;
import io.github.zap.zombies.game.perk.ZombiesPerks;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.Set;

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
    private final ZombiesHotbarManager hotbarManager;

    @Getter
    private final ZombiesPerks perks;

    private WindowData targetWindow;
    private int windowRepairTaskId = -1;

    /**
     * Creates a new ZombiesPlayer instance from the provided values.
     * @param arena The ZombiesArena this player belongs to
     * @param player The underlying Player instance
     * @param equipmentManager The equipment manager for the map equipment
     */
    public ZombiesPlayer(ZombiesArena arena, Player player, EquipmentManager equipmentManager) {
        super(arena, player);
        this.arena = arena;
        this.coins = arena.getMap().getStartingCoins();

        hotbarManager = new ZombiesHotbarManager(player);

        for (Map.Entry<String, Set<Integer>> hotbarObjectGroupSlot : arena.getMap()
                .getHotbarObjectGroupSlots().entrySet()) {
            hotbarManager.addEquipmentObjectGroup(equipmentManager
                    .createEquipmentObjectGroup(hotbarObjectGroupSlot.getKey(), player,
                    hotbarObjectGroupSlot.getValue()));
        }

        for (String equipment : arena.getMap().getDefaultEquipments()) {
            EquipmentData<?> equipmentData = equipmentManager.getEquipmentData(
                    arena.getMap().getMapNameKey(), equipment
            );
            Integer slot = hotbarManager.getHotbarObjectGroup(equipmentData.getEquipmentType()).getNextEmptySlot();

            if (slot != null) {
                hotbarManager.setHotbarObject(slot, equipmentManager.createEquipment(player, slot, equipmentData));
            }
        }

        perks = new ZombiesPerks(this);
    }

    public void quit() {
        super.quit();

        Bukkit.getScheduler().cancelTask(windowRepairTaskId);
        windowRepairTaskId = -1;

        perks.disableAll();
    }

    @Override
    public void rejoin() {
        super.rejoin();

        state = ZombiesPlayerState.DEAD;
        getPlayer().setGameMode(GameMode.SPECTATOR);
    }

    @Override
    public void dispose() {
        perks.dispose();
    }

    public void addCoins(int amount) {
        Zombies.sendLocalizedMessage(getPlayer(), MessageKey.ADD_GOLD, amount);
        coins += amount;
    }

    public void subtractCoins(int amount) {
        Zombies.sendLocalizedMessage(getPlayer(), MessageKey.SUBTRACT_GOLD, amount);
        coins -= amount;
    }

    public boolean isAlive() {
        return state == ZombiesPlayerState.ALIVE;
    }

    /**
     * Puts the player into a window repairing state.
     */
    public void activateRepair() {
        if(windowRepairTaskId == -1) {
            MapData map = arena.getMap();
            windowRepairTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Zombies.getInstance(),
                    this::checkForWindow, map.getInitialRepairDelay(), map.getWindowRepairTicks());
        }
    }

    /**
     * Disables window repair state.
     */
    public void disableRepair() {
        if(windowRepairTaskId != -1) {
            Bukkit.getScheduler().cancelTask(windowRepairTaskId);
            windowRepairTaskId = -1;
        }
    }

    /**
     * Knocks down this player.
     */
    public void knock() {
        if(state == ZombiesPlayerState.ALIVE) {
            state = ZombiesPlayerState.KNOCKED;

            hotbarManager.switchProfile(ZombiesHotbarManager.KNOCKED_DOWN_PROFILE_NAME);

            //TODO: player knockdown code
        }
    }

    /**
     * Revives this player.
     */
    public void revive() {
        if(state == ZombiesPlayerState.KNOCKED) {
            state = ZombiesPlayerState.ALIVE;

            hotbarManager.switchProfile(ZombiesHotbarManager.DEFAULT_PROFILE_NAME);

            //TODO: dead body removal code
        }
    }

    /**
     * Respawns the player at the map spawn. Also revives them, if they were knocked down.
     */
    public void respawn() {
        revive();
        state = ZombiesPlayerState.ALIVE;
        getPlayer().teleport(WorldUtils.locationFrom(arena.getWorld(), arena.getMap().getSpawn()));
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
                if(blocksRepaired > 0) { //break the actual blocks
                    for(int i = previousIndex; i <= previousIndex + blocksRepaired; i++) {
                        WorldUtils.getBlockAt(arena.getWorld(), targetWindow.getFaceVectors().get(i))
                                .setType(targetWindow.getRepairedMaterials().get(i));
                    }

                    addCoins(blocksRepaired * arena.getMap().getCoinsOnRepair());
                }
            }
            else {
                //can't repair because someone else already is
                Zombies.sendLocalizedMessage(getPlayer(), MessageKey.WINDOW_REPAIR_FAIL_PLAYER);
            }
        }
        else {
            //can't repair because there is a something attacking the window
            Zombies.sendLocalizedMessage(getPlayer(), MessageKey.WINDOW_REPAIR_FAIL_MOB);
        }
    }
}
