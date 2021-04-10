package io.github.zap.zombies.game;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.ResourceManager;
import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.game.arena.ManagedPlayer;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.hotbar.HotbarObjectGroup;
import io.github.zap.arenaapi.hotbar.HotbarProfile;
import io.github.zap.arenaapi.util.AttributeHelper;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.game.corpse.Corpse;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.RoomData;
import io.github.zap.zombies.game.data.map.WindowData;
import io.github.zap.zombies.game.data.powerups.EarnedGoldMultiplierPowerUpData;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;
import io.github.zap.zombies.game.equipment.perk.FlamingBullets;
import io.github.zap.zombies.game.equipment.perk.FrozenBullets;
import io.github.zap.zombies.game.equipment.perk.Perk;
import io.github.zap.zombies.game.hotbar.ZombiesHotbarManager;
import io.github.zap.zombies.game.powerups.EarnedGoldMultiplierPowerUp;
import io.github.zap.zombies.game.powerups.PowerUpState;
import io.github.zap.zombies.stats.CacheInformation;
import io.github.zap.zombies.stats.player.PlayerGeneralStats;
import io.github.zap.zombies.stats.player.PlayerMapStats;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Collectors;

public class ZombiesPlayer extends ManagedPlayer<ZombiesPlayer, ZombiesArena> implements Damager {

    private static final String FROZEN_BULLETS_ATTRIBUTE_NAME = "frozen_bullets_slowdown";

    @Getter
    private final ZombiesArena arena;

    @Getter
    @Setter
    private ZombiesPlayerState state = ZombiesPlayerState.ALIVE;

    private final ItemStack[] equipment;

    @Getter
    @Setter
    private String deathRoomName;

    @Getter
    private Corpse corpse;

    @Getter
    private int coins;

    @Getter
    @Setter
    private int kills;

    @Getter
    @Setter
    private int repairIncrement = 1;

    private final ResourceManager resourceManager;

    @Getter
    private final ZombiesHotbarManager hotbarManager;

    @Getter
    private final State<Double> fireRateMultiplier = new State<>(1D);

    private int frozenBulletsTaskId = -1;

    private WindowData targetWindow;

    private boolean repairOn;
    private int windowRepairTaskId = -1;

    private int boundsCheckTaskId = -1;

    private boolean reviveOn;
    private int reviveTaskId = -1;
    private Corpse targetCorpse;

    /**
     * Creates a new ZombiesPlayer instance from the provided values.
     * @param arena The ZombiesArena this player belongs to
     * @param player The underlying Player instance
     */
    public ZombiesPlayer(@NotNull ZombiesArena arena, @NotNull Player player) {
        super(arena, player);

        this.arena = arena;
        player.getInventory().clear();
        //noinspection ConstantConditions
        this.equipment = player.getEquipment().getArmorContents();
        this.coins = arena.getMap().getStartingCoins();

        //noinspection ConstantConditions
        this.hotbarManager = new ZombiesHotbarManager(getPlayer());

        setAliveState();

        resourceManager = new ResourceManager(arena.getPlugin());
    }

    public void quit() {
        super.quit();

        if(super.isInGame()) {
            state = ZombiesPlayerState.DEAD;

            disablePerks(arena.getMap().isPerksLostOnQuit());

            endTasks();
        }
    }

    @Override
    public void rejoin() {
        super.rejoin();

        state = ZombiesPlayerState.DEAD;
        setDeadState();

        //noinspection ConstantConditions
        getPlayer().getEquipment().setArmorContents(new ItemStack[4]);
        hotbarManager.switchProfile(ZombiesHotbarManager.DEAD_PROFILE_NAME);
    }

    @Override
    public void dispose() {
        resourceManager.dispose();
        endTasks();

        if (corpse != null) {
            corpse.destroy();
            corpse = null;
        }

        HotbarObjectGroup hotbarObjectGroup = hotbarManager.getHotbarObjectGroup(EquipmentObjectGroupType.PERK.name());
        for (HotbarObject hotbarObject : hotbarObjectGroup.getHotbarObjectMap().values()) {
            if (hotbarObject instanceof Perk<?, ?, ?, ?>) {
                Perk<?, ?, ?, ?> perk = (Perk<?, ?, ?, ?>) hotbarObject;

                Event<?> event = perk.getActionTriggerEvent();
                if (event != null) {
                    event.dispose();
                }
            }
        }

        if (isInGame()) {
            super.quit();
        }
    }

    public void addCoins(int amount) {
        addCoins(amount, "");
    }

    public void addCoins(int amount, String msg) {
        if(amount > 0) {
            Player player = getPlayer();

            if (player != null) {
                StringBuilder sb = new StringBuilder();
                double multiplier = 1;
                int count = 0;
                var optGM = getArena().getPowerUps().stream()
                        .filter(x -> x instanceof EarnedGoldMultiplierPowerUp && x.getState() == PowerUpState.ACTIVATED)
                        .collect(Collectors.toSet());
                if (msg != null && !msg.isEmpty()) {
                    sb.append(msg);
                    count++;
                }


                for (var item : optGM) {
                    if (count != 0)
                        sb.append(ChatColor.RESET).append(ChatColor.GOLD).append(", ");
                    sb.append(ChatColor.RESET).append(item.getData().getDisplayName());
                    multiplier *= ((EarnedGoldMultiplierPowerUpData) item.getData()).getMultiplier();
                    count++;
                }

                var fullMsg = sb.append(ChatColor.RESET).append(ChatColor.GOLD).toString();
                amount *= multiplier;
                if (ChatColor.stripColor(fullMsg).isEmpty())
                    getPlayer().sendMessage(String.format("%s+%d Gold!", ChatColor.GOLD, amount));
                else
                    getPlayer().sendMessage(String.format("%s+%d Gold (%s)!", ChatColor.GOLD, amount, fullMsg));
            }

            // Still add coins even if player is gone
            // integer overflow check
            if(Integer.MAX_VALUE - coins - amount > 0)
                coins += amount;
            else
                coins = Integer.MAX_VALUE;
        }
    }

    public void subtractCoins(int amount) {
        if(amount > 0) {
            Player player = getPlayer();
            if (player != null) {
                player.sendMessage(String.format("%s-%d Gold", ChatColor.GOLD, amount));
            }
            coins -= amount;
        }
    }

    public void setCoins(int amount) {
        coins = Math.max(0, amount);
    }

    /**
     * Updates the player's equipment
     * @param newEquipment The player's new equipment
     */
    public void updateEquipment(ItemStack[] newEquipment) {
        System.arraycopy(newEquipment, 0, equipment, 0, newEquipment.length);
        if (isAlive() && isInGame()) {
            //noinspection ConstantConditions
            getPlayer().getEquipment().setArmorContents(equipment);
        }
    }

    public boolean isAlive() {
        return state == ZombiesPlayerState.ALIVE;
    }

    /**
     * Starts tasks related to when the player shifts
     */
    public void startTasks() {
        if (windowRepairTaskId == -1) {
            windowRepairTaskId = arena.runTaskTimer(0, arena.getMap().getWindowRepairTicks(), this::checkForWindow).getTaskId();
        }

        if (reviveTaskId == -1) {
            reviveTaskId = arena.runTaskTimer(0, 2L, this::checkForCorpses).getTaskId();
        }

        if(boundsCheckTaskId == -1) {
            boundsCheckTaskId = arena.runTaskTimer(0, 5, this::ensureInBounds).getTaskId();
        }
    }

    /**
     * Ends tasks related to when the player shifts
     */
    public void endTasks() {
        if (windowRepairTaskId != -1) {
            Bukkit.getScheduler().cancelTask(windowRepairTaskId);
            windowRepairTaskId = -1;
        }

        if (reviveTaskId != -1) {
            Bukkit.getScheduler().cancelTask(reviveTaskId);
            reviveTaskId = -1;
        }

        if(boundsCheckTaskId != -1) {
            Bukkit.getScheduler().cancelTask(boundsCheckTaskId);
            boundsCheckTaskId = -1;
        }
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
        reviveOn = true;
    }

    /**
     * Disables revive state
     */
    public void disableRevive() {
        if (targetCorpse != null) {
            Player player = getPlayer();
            if (player != null) {
                getPlayer().sendActionBar(Component.empty());
            }
            targetCorpse.setReviver(null);
            targetCorpse = null;
        }

        reviveOn = false;
    }

    /**
     * Knocks down this player.
     */
    public void knock() {
        if(isAlive() && isInGame()) {
            state = ZombiesPlayerState.KNOCKED;

            hotbarManager.switchProfile(ZombiesHotbarManager.KNOCKED_DOWN_PROFILE_NAME);

            corpse = new Corpse(this);

            disableRepair();
            disableRevive();

            getArena().getStatsManager().queueCacheModification(CacheInformation.PLAYER,
                    getOfflinePlayer().getUniqueId(), (stats) -> {
                PlayerMapStats mapStats = stats.getMapStatsForMap(getArena().getMap());
                mapStats.setKnockDowns(mapStats.getKnockDowns() + 1);
            }, PlayerGeneralStats::new);

            setKnockedState();
        }
    }

    /**
     * Commits murder. 😈
     */
    public void kill() {
        if (state == ZombiesPlayerState.KNOCKED && isInGame()) {
            state = ZombiesPlayerState.DEAD;

            disablePerks(arena.getMap().isPerksLostOnDeath());

            hotbarManager.switchProfile(ZombiesHotbarManager.DEAD_PROFILE_NAME);

            Location corpseLocation = corpse.getLocation();
            for (Player player : getArena().getWorld().getPlayers()) {
                player.playSound(Sound.sound(
                        Key.key("minecraft:entity.player.hurt"),
                        Sound.Source.MASTER,
                        1.0F,
                        1.0F
                ), corpseLocation.getX(), corpseLocation.getY(), corpseLocation.getZ());
            }

            getArena().getStatsManager().queueCacheModification(CacheInformation.PLAYER,
                    getOfflinePlayer().getUniqueId(), (stats) -> {
                PlayerMapStats mapStats = stats.getMapStatsForMap(getArena().getMap());
                mapStats.setDeaths(mapStats.getDeaths() + 1);
            }, PlayerGeneralStats::new);

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

            enablePerks();
            setAliveState();

            Player player = getPlayer();
            if (player != null && getPlayer().isSneaking()) {
                activateRepair();
                activateRevive();
            }
        }
    }

    /**
     * Respawns the player at the map spawn. Also revives them, if they were knocked down.
     */
    public void respawn() {
        Player player = getPlayer();
        if (player != null && isInGame()) {
            revive();
            getPlayer().teleport(WorldUtils.locationFrom(arena.getWorld(), arena.getMap().getSpawn()));
        }
    }

    /**
     * Increases the player's kill counter
     * @param kills The number of kills to add
     */
    public void addKills(int kills) {
        this.kills += kills;
    }

    @Override
    public void onDealsDamage(@NotNull DamageAttempt attempt, @NotNull Mob damaged, double deltaHealth) {
        Player player = getPlayer();
        if (player != null) {
            int coins = attempt.getCoins(this, damaged);

            if (attempt.ignoresArmor(this, damaged)) {
                addCoins(coins, "Critical Hit");
            } else {
                addCoins(coins);
            }

            player.playSound(Sound.sound(
                    Key.key("minecraft:entity.arrow.hit_player"),
                    Sound.Source.MASTER,
                    1.0F,
                    attempt.ignoresArmor(this, damaged) ? 1.5F : 2F
            ));

            if (damaged.getHealth() <= 0) {
                getArena().getStatsManager().queueCacheModification(CacheInformation.PLAYER, player.getUniqueId(),
                        (stats) -> {
                    PlayerMapStats mapStats = stats.getMapStatsForMap(getArena().getMap());
                    mapStats.setKills(mapStats.getKills() + 1);
                    }, PlayerGeneralStats::new);

                addKills(1);
            } else {
                HotbarObjectGroup hotbarObjectGroup = hotbarManager
                        .getHotbarObjectGroup(EquipmentObjectGroupType.PERK.name());
                for (HotbarObject hotbarObject : hotbarObjectGroup.getHotbarObjectMap().values()) {
                    if (hotbarObject instanceof FrozenBullets) {
                        FrozenBullets frozenBullets = (FrozenBullets) hotbarObject;

                        AttributeInstance speed = damaged.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);

                        if (speed != null) {
                            Optional<AttributeModifier> optionalAttributeModifier
                                    = AttributeHelper.getModifier(speed, FROZEN_BULLETS_ATTRIBUTE_NAME);

                            if (optionalAttributeModifier.isPresent()) {
                                Bukkit.getScheduler().cancelTask(frozenBulletsTaskId);

                                frozenBulletsTaskId = arena.runTaskLater(frozenBullets.getDuration(),
                                        () -> speed.removeModifier(optionalAttributeModifier.get())).getTaskId();
                            } else {
                                AttributeModifier modifier = new AttributeModifier(FROZEN_BULLETS_ATTRIBUTE_NAME,
                                        -frozenBullets.getReducedSpeed(), AttributeModifier.Operation.ADD_SCALAR);

                                speed.addModifier(modifier);

                                frozenBulletsTaskId = arena.runTaskLater(frozenBullets.getDuration(),
                                        () -> speed.removeModifier(modifier)).getTaskId();
                            }
                        }
                    } else if (hotbarObject instanceof FlamingBullets) {
                        FlamingBullets flamingBullets = (FlamingBullets) hotbarObject;
                        damaged.setFireTicks(flamingBullets.getDuration());
                    }
                }
            }
        }
    }

    /**
     * Enables all the player's perks
     */
    public void enablePerks() {
        HotbarProfile defaultProfile = hotbarManager.getProfiles().get(HotbarManager.DEFAULT_PROFILE_NAME);
        HotbarObjectGroup hotbarObjectGroup = hotbarManager.getHotbarObjectGroup(defaultProfile,
                EquipmentObjectGroupType.PERK.name());

        for (Integer slot : hotbarObjectGroup.getHotbarObjectMap().keySet()) {
            HotbarObject hotbarObject = hotbarObjectGroup.getHotbarObject(slot);
            if (hotbarObject instanceof Perk<?, ?, ?, ?>) {
                Perk<?, ?, ?, ?> perk = (Perk<?, ?, ?, ?>) hotbarObject;
                perk.activate();
            }
        }
    }

    /**
     * Disables all the player's perks
     * @param remove Whether the perks should be reset
     */
    public void disablePerks(boolean remove) {
        HotbarProfile defaultProfile = hotbarManager.getProfiles().get(HotbarManager.DEFAULT_PROFILE_NAME);
        HotbarObjectGroup hotbarObjectGroup = hotbarManager.getHotbarObjectGroup(defaultProfile,
                EquipmentObjectGroupType.PERK.name());

        if (remove) {
            for (Integer slot : hotbarObjectGroup.getHotbarObjectMap().keySet()) {
                hotbarObjectGroup.remove(slot, true);
            }
        } else {
            for (Integer slot : hotbarObjectGroup.getHotbarObjectMap().keySet()) {
                HotbarObject hotbarObject = hotbarObjectGroup.getHotbarObject(slot);
                if (hotbarObject instanceof Perk<?, ?, ?, ?>) {
                    Perk<?, ?, ?, ?> perk = (Perk<?, ?, ?, ?>) hotbarObject;
                    perk.deactivate();;
                }
            }
        }
    }

    /**
     * Tries to find and repair a window.
     */
    private void checkForWindow() {
        MapData map = arena.getMap();
        Player player = getPlayer();

        if (player != null) {
            if (targetWindow == null) { //our target window is null, so look for one to repair
                WindowData window = map.windowMatching(windowData -> !windowData.isFullyRepaired(arena)
                        && windowData.getRepairingPlayerProperty().getValue(arena) == null
                        && windowData.inRange(getPlayer().getLocation().toVector(), arena.getMap().getWindowRepairRadiusSquared()));

                if (window != null) {
                    if (repairOn) {
                        targetWindow = window;
                        tryRepairWindow(targetWindow); //directly repair window; no need to perform checks
                        player.sendActionBar(Component.text());
                    } else {
                        player.sendActionBar(Component.text("Hold SHIFT to repair!")
                                .color(NamedTextColor.YELLOW));
                    }
                } else {
                    player.sendActionBar(Component.text());
                }
            } else { //we already have a target window - make sure it's still in range
                if (targetWindow.inRange(getPlayer().getLocation().toVector(), map.getWindowRepairRadiusSquared())
                        && repairOn && isAlive()) {
                    tryRepairWindow(targetWindow);
                } else {
                    targetWindow = null;
                }
            }
        }
    }

    private void ensureInBounds() {
        MapData map = arena.getMap();

        Player bukkitPlayer = getPlayer();

        if(bukkitPlayer != null) {
            RoomData roomIn = map.roomAt(getPlayer().getLocation().toVector());

            if(roomIn != null) {
                for(WindowData windowData : roomIn.getWindows()) {
                    if(windowData.playerInside(getPlayer().getLocation().toVector())) {
                        Player player = getPlayer();
                        Location current = player.getLocation();
                        Vector target = windowData.getTarget();
                        player.teleport(new Location(arena.getWorld(), target.getX(), target.getY(), target.getZ(),
                                current.getYaw(), current.getPitch()));
                    }
                }
            }
        }
    }

    /**
     * Attempts to repair the given window.
     */
    private void tryRepairWindow(WindowData targetWindow) {
        Player player = getPlayer();

        if (player != null) {
            Property<Entity> attackingEntityProperty = targetWindow.getAttackingEntityProperty();
            Entity attacker = attackingEntityProperty.getValue(arena);

            if (attacker == null || attacker.isDead()) {
                attackingEntityProperty.setValue(arena, null);

                Property<ZombiesPlayer> currentRepairerProperty = targetWindow.getRepairingPlayerProperty();
                ZombiesPlayer currentRepairer = currentRepairerProperty.getValue(arena);

                if (currentRepairer == null || !currentRepairer.isAlive()) {
                    currentRepairer = this;
                    currentRepairerProperty.setValue(arena, this);
                }

                if (currentRepairer == this) {
                    //advance repair state
                    int previousIndex = targetWindow.getCurrentIndexProperty().getValue(arena);
                    int blocksRepaired = targetWindow.advanceRepairState(arena, repairIncrement);
                    for (int i = previousIndex; i < previousIndex + blocksRepaired; i++) {
                        Block target = WorldUtils.getBlockAt(arena.getWorld(), targetWindow.getFaceVectors().get(i + 1));
                        target.setBlockData(Bukkit.createBlockData(targetWindow.getRepairedData().get(i + 1)));

                        Vector center = targetWindow.getCenter();
                        if (i < targetWindow.getVolume() - 2) {
                            arena.getWorld().playSound(targetWindow.getBlockRepairSound(), center.getX(), center.getY(), center.getZ());
                        } else {
                            arena.getStatsManager().queueCacheModification(CacheInformation.PLAYER,
                                    player.getUniqueId(), (stats) -> {
                                PlayerMapStats mapStats = stats.getMapStatsForMap(arena.getMap());
                                mapStats.setWindowsRepaired(mapStats.getWindowsRepaired() + 1);
                            }, PlayerGeneralStats::new);
                            arena.getWorld().playSound(targetWindow.getWindowRepairSound(), center.getX(), center.getY(), center.getZ());
                        }
                    }

                    addCoins(blocksRepaired * arena.getMap().getCoinsOnRepair());
                } else {
                    getPlayer().sendMessage(ChatColor.RED + "Someone is already repairing that window!");
                }
            } else {
                getPlayer().sendMessage(ChatColor.RED + "A mob is attacking that window!");
            }
        }
    }
    /**
     * Checks for corpses to revive or continues reviving the current corpse
     */
    private void checkForCorpses() {
        Player player = getPlayer();
        if (player != null && isAlive()) {
            int maxDistance = arena.getMap().getReviveRadius();

            if (targetCorpse == null) {
                selectNewCorpse();
            } else if (!targetCorpse.isActive()) {
                getPlayer().sendActionBar(Component.text());
                targetCorpse = null;

                selectNewCorpse();
            } else {
                double distance
                        = getPlayer().getLocation().toVector().distanceSquared(targetCorpse.getLocation().toVector());

                if (distance < maxDistance && reviveOn) {
                    targetCorpse.continueReviving();
                } else {
                    getPlayer().sendActionBar(Component.text());
                    targetCorpse.setReviver(null);
                    targetCorpse = null;

                    selectNewCorpse();
                }
            }
        } else if (targetCorpse != null) {
            targetCorpse.setReviver(null);
        }
    }

    /**
     * Finds a new corpse to revive
     */
    private void selectNewCorpse() {
        Player player = getPlayer();

        if (player != null) {
            int maxDistance = arena.getMap().getReviveRadius();

            for (Corpse corpse : arena.getAvailableCorpses()) {
                double distance
                        = player.getLocation().toVector().distanceSquared(corpse.getLocation().toVector());
                if (distance <= maxDistance) {
                    Player corpseBukkitPlayer = corpse.getZombiesPlayer().getPlayer();
                    if (corpseBukkitPlayer != null) {
                        if (reviveOn) {
                            targetCorpse = corpse;
                            targetCorpse.setReviver(this);
                            targetCorpse.continueReviving();
                        } else {
                            getPlayer().sendActionBar(Component
                                    .text(String.format("Hold SHIFT to Revive %s!", corpseBukkitPlayer.getName()))
                                    .color(NamedTextColor.YELLOW)
                            );
                        }

                        break;
                    }
                }
            }
        }
    }

    public void setKnockedState() {
        Player player = getPlayer();

        if(player != null) {
            ArenaApi.getInstance().applyDefaultCondition(player);
            //noinspection ConstantConditions
            player.getEquipment().setArmorContents(new ItemStack[4]);
            player.setWalkSpeed(0);
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128, false,
                    false, false));
            player.setInvulnerable(true);
            player.setGameMode(GameMode.ADVENTURE);
            getArena().getHiddenPlayers().add(player);
            endTasks();
        }
    }

    public void setAliveState() {
        Player player = getPlayer();

        if(player != null) {
            ArenaApi.getInstance().applyDefaultCondition(player);
            //noinspection ConstantConditions
            player.getEquipment().setArmorContents(equipment);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, 2, false,
                    false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, false, false, false));
            player.setInvulnerable(false);
            player.setGameMode(GameMode.ADVENTURE);
            startTasks();
            getArena().getHiddenPlayers().remove(player);
        }
    }

    public void setDeadState() {
        Player player = getPlayer();

        if(player != null) {
            ArenaApi.getInstance().applyDefaultCondition(player);
            player.setAllowFlight(true);
            player.setCollidable(false);
            player.setGameMode(GameMode.ADVENTURE);
            getArena().getHiddenPlayers().add(player);
        }
    }
}
