package io.github.zap.zombies.game;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.game.arena.ManagedPlayer;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.corpse.Corpse;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.equipment.EquipmentManager;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.WindowData;
import io.github.zap.zombies.game.data.powerups.EarnedGoldMultiplierPowerUpData;
import io.github.zap.zombies.game.hotbar.ZombiesHotbarManager;
import io.github.zap.zombies.game.perk.ZombiesPerks;
import io.github.zap.zombies.game.powerups.EarnedGoldMultiplierPowerUp;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ZombiesPlayer extends ManagedPlayer<ZombiesPlayer, ZombiesArena> {
    
    @Getter
    private final ZombiesArena arena;

    @Getter
    @Setter
    private ZombiesPlayerState state = ZombiesPlayerState.ALIVE;

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

    private boolean repairOn;
    private int windowRepairTaskId;

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

        hotbarManager = new ZombiesHotbarManager(getPlayer());

        for (Map.Entry<String, Set<Integer>> hotbarObjectGroupSlot : arena.getMap()
                .getHotbarObjectGroupSlots().entrySet()) {
            hotbarManager.addEquipmentObjectGroup(equipmentManager
                    .createEquipmentObjectGroup(hotbarObjectGroupSlot.getKey(), getPlayer(),
                    hotbarObjectGroupSlot.getValue()));
        }

        for(String equipment : arena.getMap().getDefaultEquipments()) {
            EquipmentData<?> equipmentData = equipmentManager.getEquipmentData(arena.getMap().getName(), equipment);
            Integer slot = hotbarManager.getHotbarObjectGroup(equipmentData.getEquipmentType()).getNextEmptySlot();

            if (slot != null) {
                hotbarManager.setHotbarObject(
                        slot,
                        equipmentManager.createEquipment(arena, this, slot, equipmentData)
                );
            }
        }

        perks = new ZombiesPerks(this);
        windowRepairTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Zombies.getInstance(),
                this::checkForWindow, 0, arena.getMap().getWindowRepairTicks());
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
        perks.activateAll();
        setDeadState();
    }

    @Override
    public void dispose() {
        perks.dispose();

        Bukkit.getScheduler().cancelTask(windowRepairTaskId);
        Bukkit.getScheduler().cancelTask(reviveTaskId);
        ArenaApi.getInstance().applyDefaultCondition(getPlayer());
    }

    public void addCoins(int amount) {
        addCoins(amount, "");
    }

    public void addCoins(int amount, String msg) {
        if(amount > 0) {
            StringBuilder sb = new StringBuilder();
            double multiplier = 1;
            int count = 0;
            var optGM = getArena().getPowerUps().stream()
                    .filter(x -> x instanceof EarnedGoldMultiplierPowerUp)
                    .collect(Collectors.toSet());
            if(msg != null && !msg.isEmpty()) {
                sb.append(msg);
                count++;
            }


            for (var item : optGM) {
                if(count != 0)
                    sb.append(ChatColor.RESET).append(ChatColor.GOLD).append(", ");
                sb.append(ChatColor.RESET).append(item.getData().getDisplayName());
                multiplier *= ((EarnedGoldMultiplierPowerUpData)item.getData()).getMultiplier();
                count ++;
            }

            var fullMsg = sb.append(ChatColor.RESET).append(ChatColor.GOLD).toString();
            amount *= multiplier;
            if(ChatColor.stripColor(fullMsg).isEmpty())
                getPlayer().sendMessage(String.format("%s+%d Gold!", ChatColor.GOLD, amount));
            else
                getPlayer().sendMessage(String.format("%s+%d Gold (%s)!", ChatColor.GOLD, amount, fullMsg));

            coins += amount;
        }
    }

    public void subtractCoins(int amount) {
        getPlayer().sendMessage(String.format("%s-%d Gold", ChatColor.GOLD, amount));
        coins -= amount;
    }

    public boolean isAlive() {
        return state == ZombiesPlayerState.ALIVE;
    }

    /**
     * Puts the player into a window repairing state.
     */
    public void activateRepair() {
        repairOn = true;
    }

    /**
     * Disables window repair state.
     */
    public void disableRepair() {
        if(targetWindow != null) {
            Property<ZombiesPlayer> repairingPlayerProperty = targetWindow.getRepairingPlayerProperty();
            if(repairingPlayerProperty.getValue(arena) == this) {
                repairingPlayerProperty.setValue(arena, null);
            }
        }

        repairOn = false;
    }

    /**
     * Puts the player into a reviving state
     */
    public void activateRevive() {
        if (reviveTaskId == -1) {
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
            if (targetCorpse != null) {
                targetCorpse.setReviver(null);
                targetCorpse = null;
            }
        }
    }

    /**
     * Knocks down this player.
     */
    public void knock() {
        if(isAlive() && isInGame()) {
            state = ZombiesPlayerState.KNOCKED;

            hotbarManager.switchProfile(ZombiesHotbarManager.KNOCKED_DOWN_PROFILE_NAME);
            corpse = new Corpse(this);

            getPerks().disableAll();

            disableRepair();
            disableRevive();

            setKnockedState();
        }
    }

    /**
     * Commits murder. 😈
     */
    public void kill() {
        if (state == ZombiesPlayerState.KNOCKED && isInGame()) {
            state = ZombiesPlayerState.DEAD;
            hotbarManager.switchProfile(ZombiesHotbarManager.DEAD_PROFILE_NAME);
            setDeadState();
        }
    }

    /**
     * Revives this player.
     */
    public void revive() {
        if (!isAlive() && isInGame()) {
            state = ZombiesPlayerState.ALIVE;

            hotbarManager.switchProfile(ZombiesHotbarManager.DEFAULT_PROFILE_NAME);

            if (corpse != null) {
                corpse.destroy();
                corpse = null;
            }

            getPerks().activateAll();
            setAliveState();

            if (getPlayer().isSneaking()) {
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
            getPlayer().teleport(WorldUtils.locationFrom(arena.getWorld(), arena.getMap().getSpawn()));
        }
    }

    /**
     * Increments the player's kill counter
     */
    public void incrementKills() {
        kills++;
    }

    /**
     * Tries to find and repair a window.
     */
    private void checkForWindow() {
        if(repairOn) {
            MapData map = arena.getMap();

            if(targetWindow == null) { //our target window is null, so look for one
                WindowData window = map.windowAtRange(getPlayer().getLocation().toVector(), map.getWindowRepairRadiusSquared());

                if(window != null) {
                    targetWindow = window;
                    tryRepairWindow(targetWindow);
                }
            }
            else { //we already have a target window - make sure it's still in range
                if(targetWindow.inRange(getPlayer().getLocation().toVector(), map.getWindowRepairRadiusSquared())) {
                    tryRepairWindow(targetWindow);
                }
                else {
                    targetWindow = null;
                }
            }
        }
    }

    /**
     * Attempts to repair the given window.
     */
    private void tryRepairWindow(WindowData targetWindow) {
        Property<Entity> attackingEntityProperty = targetWindow.getAttackingEntityProperty();
        Entity attacker = attackingEntityProperty.getValue(arena);

        if(attacker == null || attacker.isDead()) {
            attackingEntityProperty.setValue(arena, null);

            Property<ZombiesPlayer> currentRepairerProperty = targetWindow.getRepairingPlayerProperty();
            ZombiesPlayer currentRepairer = currentRepairerProperty.getValue(arena);

            if(currentRepairer == null || !currentRepairer.isAlive()) {
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
                        arena.getWorld().playSound(centerLocation, targetWindow.getBlockRepairSound(), SoundCategory.BLOCKS, 5.0F, 1.0F);
                    }
                    else {
                        arena.getWorld().playSound(centerLocation, targetWindow.getWindowRepairSound(), SoundCategory.BLOCKS, 5.0F, 1.0F);
                    }
                }

                addCoins(blocksRepaired * arena.getMap().getCoinsOnRepair());
            }
            else {
                getPlayer().sendMessage(ChatColor.RED + "Someone is already repairing that window!");
            }
        }
        else {
            getPlayer().sendMessage(ChatColor.RED + "A mob is attacking that window!");
        }
    }

    /**
     * Checks for corpses to revive or continues reviving the current corpse
     */
    private void checkForCorpses() {
        int maxDistance = arena.getMap().getReviveRadius();

        if (targetCorpse == null) {
            selectNewCorpse();
        } else if (!targetCorpse.isActive()) {
            targetCorpse = null;
            selectNewCorpse();
        } else {
            double distance
                    = getPlayer().getLocation().toVector().distanceSquared(targetCorpse.getLocation().toVector());

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
            double distance
                    = getPlayer().getLocation().toVector().distanceSquared(corpse.getLocation().toVector());
            if (distance <= maxDistance) {
                targetCorpse = corpse;
                targetCorpse.setReviver(this);
                targetCorpse.continueReviving();
                break;
            }
        }
    }

    public void setKnockedState() {
        Player player = getPlayer();
        ArenaApi.getInstance().applyDefaultCondition(player);
        player.setWalkSpeed(0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128, false,
                false, false));
        player.setInvulnerable(true);
        player.setInvisible(true);
    }

    public void setAliveState() {
        Player player = getPlayer();
        ArenaApi.getInstance().applyDefaultCondition(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, 2, false,
                false, false));
        player.setInvulnerable(false);
    }

    public void setDeadState() {
        Player player = getPlayer();
        ArenaApi.getInstance().applyDefaultCondition(player);
        player.setFlying(true);
        player.setInvisible(true);
    }
}
