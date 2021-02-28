package io.github.zap.zombies.game;

import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.game.arena.ManagedPlayer;
import io.github.zap.arenaapi.util.VectorUtils;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.MessageKey;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.corpse.Corpse;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.equipment.EquipmentManager;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.WindowData;
import io.github.zap.zombies.game.hotbar.ZombiesHotbarManager;
import io.github.zap.zombies.game.perk.PerkType;
import io.github.zap.zombies.game.perk.ZombiesPerks;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Set;

public class ZombiesPlayer extends ManagedPlayer<ZombiesPlayer, ZombiesArena> {
    @Getter
    private final ZombiesArena arena;

    @Getter
    @Setter
    private ZombiesPlayerState state;

    private Corpse corpse;

    @Setter
    @Getter
    private int coins;

    @Getter
    @Setter
    private int kills;

    @Getter
    @Setter
    private int repairIncrement = 1;

    @Getter
    private final ZombiesHotbarManager hotbarManager;

    // Allow other class to modify the fire rate without modify the logic code itself. Only support
    // Mollification and Division. Also why remove old multiplier Thamid?
    @Getter
    @Setter
    private double fireRateMultiplier = 1;

    @Getter
    private final ZombiesPerks perks;

    private WindowData targetWindow;
    private int windowRepairTaskId = -1;

    private Corpse targetCorpse;
    private int reviveTaskId;

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
                hotbarManager.setHotbarObject(slot, equipmentManager.createEquipment(this, slot, equipmentData));
            }
        }

        perks = new ZombiesPerks(this);
    }

    public void quit() {
        super.quit();

        state = ZombiesPlayerState.DEAD;
        Bukkit.getScheduler().cancelTask(windowRepairTaskId);
        windowRepairTaskId = -1;

        perks.disableAll();
    }

    @Override
    public void rejoin() {
        super.rejoin();

        state = ZombiesPlayerState.DEAD;
        getPlayer().setGameMode(GameMode.SPECTATOR);

        perks.activateAll();
    }

    @Override
    public void dispose() {
        perks.dispose();
    }

    public void addCoins(int amount) {
        if(amount > 0) {
            Zombies.sendLocalizedMessage(getPlayer(), MessageKey.ADD_GOLD, amount);
            coins += amount;
        }
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
                    this::checkForWindow, 0, map.getWindowRepairTicks());
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
     * Puts the player into a reviving state
     */
    public void activateRevive() {
        if (reviveTaskId == -1) {
            MapData map = arena.getMap();
            reviveTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                    Zombies.getInstance(),
                    this::checkForCorpses,
                    0L,
                    2L
            );
        }
    }

    /**
     * Disables revive state
     */
    public void disableRevive() {
        if (reviveTaskId != -1) {
            Bukkit.getScheduler().cancelTask(reviveTaskId);
            reviveTaskId = -1;
        }
    }

    /**
     * Knocks down this player.
     */
    public void knock() {
        if(state == ZombiesPlayerState.ALIVE && isInGame()) {
            state = ZombiesPlayerState.KNOCKED;

            hotbarManager.switchProfile(ZombiesHotbarManager.KNOCKED_DOWN_PROFILE_NAME);
            corpse = new Corpse(this);

            getPerks().getPerk(PerkType.SPEED).disable();
            Player player = getPlayer();
            player.setWalkSpeed(0);
            player.setInvisible(true);
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128,
                    true, false, false));

            disableRepair();
            disableRevive();
        }
    }

    /**
     * Commits murder. 😈
     */
    public void kill() {
        if (state == ZombiesPlayerState.KNOCKED && isInGame()) {
            state = ZombiesPlayerState.DEAD;

            hotbarManager.switchProfile(ZombiesHotbarManager.DEAD_PROFILE_NAME);

            Player player = getPlayer();
            player.setWalkSpeed(0.2F);
            player.removePotionEffect(PotionEffectType.JUMP);
            player.setAllowFlight(true);
            player.setFlying(true);
        }
    }

    /**
     * Revives this player.
     */
    public void revive() {
        if(state == ZombiesPlayerState.KNOCKED && isInGame()) {
            state = ZombiesPlayerState.ALIVE;

            hotbarManager.switchProfile(ZombiesHotbarManager.DEFAULT_PROFILE_NAME);

            if (corpse != null) {
                corpse.destroy();
                corpse = null;
            }

            getPerks().getPerk(PerkType.SPEED).activate();
            Player player = getPlayer();
            player.removePotionEffect(PotionEffectType.JUMP);
            player.setInvisible(false);
            player.setFlying(false);
            player.setAllowFlight(false);

            if (player.isSneaking()) {
                activateRepair();
                activateRevive();
            }
        }
    }

    /**
     * Respawns the player at the map spawn. Also revives them, if they were knocked down.
     */
    public void respawn() {
        if(isInGame()) {
            revive();
            state = ZombiesPlayerState.ALIVE;
            getPlayer().teleport(WorldUtils.locationFrom(arena.getWorld(), arena.getMap().getSpawn()));
        }
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

        if(attackingEntityProperty.getValue(arena) == null) {
            Property<ZombiesPlayer> currentRepairerProperty = targetWindow.getRepairingPlayerProperty();
            ZombiesPlayer currentRepairer = currentRepairerProperty.getValue(arena);

            if(currentRepairer == null) {
                currentRepairer = this;
                currentRepairerProperty.setValue(arena, this);
            }

            if(currentRepairer == this) {
                //advance repair state
                int previousIndex = targetWindow.getCurrentIndexProperty().getValue(arena);
                int blocksRepaired = targetWindow.advanceRepairState(arena, repairIncrement);
                for(int i = previousIndex; i < previousIndex + blocksRepaired; i++) {
                    WorldUtils.getBlockAt(arena.getWorld(), targetWindow.getFaceVectors().get(i + 1))
                            .setType(targetWindow.getRepairedMaterials().get(i + 1));

                    Vector center = targetWindow.getCenter();
                    Location centerLocation = new Location(arena.getWorld(), center.getX(), center.getY(), center.getZ());
                    if(i < targetWindow.getVolume() - 2) {
                        arena.getWorld().playSound(centerLocation, targetWindow.getBlockRepairSound(), SoundCategory.BLOCKS, 10.0F, 10.0F);
                    }
                    else {
                        arena.getWorld().playSound(centerLocation, targetWindow.getWindowRepairSound(), SoundCategory.BLOCKS, 10.0F, 10.0F);
                    }
                }

                //addCoins(blocksRepaired * arena.getMap().getCoinsOnRepair());
            }
            else {
                //can't repair because someone else already is
                //Zombies.sendLocalizedMessage(getPlayer(), MessageKey.WINDOW_REPAIR_FAIL_PLAYER);
            }
        }
        else {
            //can't repair because there is a something attacking the window
            //Zombies.sendLocalizedMessage(getPlayer(), MessageKey.WINDOW_REPAIR_FAIL_MOB);
        }
    }

    /**
     * Checks for corpses to revive or continues reviving the current corpse
     */
    private void checkForCorpses() {
        int maxDistance = arena.getMap().getReviveRadius();

        if (targetCorpse == null || !targetCorpse.isActive()) {
            selectNewCorpse();
        } else {
            double distance = VectorUtils.manhattanDistance(
                    getPlayer().getLocation().toVector(),
                    targetCorpse.getLocation().toVector()
            );

            if (distance < maxDistance) {
                targetCorpse.continueReviving();
            } else {
                targetCorpse.setReviver(null);
                targetCorpse = null;

                selectNewCorpse();
            }
        }
    }

    /**
     * Finds a new corpse to revive
     */
    private void selectNewCorpse() {
        int maxDistance = arena.getMap().getReviveRadius();

        for (Corpse corpse : arena.getAvailableCorpses()) {
            double distance = VectorUtils.manhattanDistance(
                    getPlayer().getLocation().toVector(),
                    corpse.getLocation().toVector()
            );
            if (distance <= maxDistance) {
                targetCorpse = corpse;
                targetCorpse.setReviver(this);
                targetCorpse.continueReviving();
                break;
            }
        }
    }

}
