package io.github.zap.zombies.game.player;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.ResourceManager;
import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.game.arena.ManagedPlayer;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.hotbar.HotbarObjectGroup;
import io.github.zap.arenaapi.hotbar.HotbarProfile;
import io.github.zap.arenaapi.pathfind.PathTarget;
import io.github.zap.arenaapi.util.AttributeHelper;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.game.*;
import io.github.zap.zombies.game.corpse.Corpse;
import io.github.zap.zombies.game.data.powerups.EarnedGoldMultiplierPowerUpData;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;
import io.github.zap.zombies.game.equipment.perk.FlamingBullets;
import io.github.zap.zombies.game.equipment.perk.FrozenBullets;
import io.github.zap.zombies.game.equipment.perk.Perk;
import io.github.zap.zombies.game.hotbar.ZombiesHotbarManager;
import io.github.zap.zombies.game.player.task.BoundsCheckTask;
import io.github.zap.zombies.game.player.task.ReviveTask;
import io.github.zap.zombies.game.player.task.WindowRepairTask;
import io.github.zap.zombies.game.powerups.EarnedGoldMultiplierPowerUp;
import io.github.zap.zombies.game.powerups.PowerUpState;
import io.github.zap.zombies.game.task.ZombiesTask;
import io.github.zap.zombies.stats.CacheInformation;
import io.github.zap.zombies.stats.player.PlayerGeneralStats;
import io.github.zap.zombies.stats.player.PlayerMapStats;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ZombiesPlayer extends ManagedPlayer<ZombiesPlayer, ZombiesArena> implements Damager, PathTarget {

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

    @Getter
    private final List<ZombiesTask> tasks = new ArrayList<>();

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

        addTasks();

        setAliveState();

        resourceManager = new ResourceManager(arena.getPlugin());
    }

    public void quit() {
        if (isInGame()) {
            kill();
            disablePerks(arena.getMap().isPerksLostOnQuit());
            endTasks();
        }

        super.quit();
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

        for (HotbarProfile hotbarProfile : hotbarManager.getProfiles().values()) {
            HotbarObjectGroup hotbarObjectGroup = hotbarManager.
                    getHotbarObjectGroup(hotbarProfile, EquipmentObjectGroupType.PERK.name());

            if (hotbarObjectGroup != null) {
                for (HotbarObject hotbarObject : hotbarObjectGroup.getHotbarObjectMap().values()) {
                    if (hotbarObject instanceof Perk<?, ?, ?, ?> perk && perk.getActionTriggerEvent() != null) {
                        Event<?> event = perk.getActionTriggerEvent();
                        if (event != null) {
                            event.dispose();
                        }
                    }
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

                String fullMsg = sb.append(ChatColor.RESET).append(ChatColor.GOLD).toString();
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
     * Starts all tasks
     */
    public void startTasks() {
        for (ZombiesTask zombiesTask : tasks) {
            zombiesTask.start();
        }
    }

    /**
     * Ends all tasks
     */
    public void endTasks() {
        for (ZombiesTask zombiesTask : tasks) {
            zombiesTask.stop();
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

            for (ZombiesTask zombiesTask : tasks) {
                zombiesTask.notifyChange();
            }

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

            for (ZombiesTask zombiesTask : tasks) {
                zombiesTask.notifyChange();
            }

            Location corpseLocation = corpse.getLocation();
            for (Player player : getArena().getWorld().getPlayers()) {
                player.playSound(Sound.sound(
                        Key.key("minecraft:entity.player.hurt"),
                        Sound.Source.MASTER,
                        1.0F,
                        1.0F
                ), corpseLocation.getX(), corpseLocation.getY(), corpseLocation.getZ());
            }
            corpse.terminate();

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

            for (ZombiesTask zombiesTask : tasks) {
                zombiesTask.notifyChange();
            }

            enablePerks();
            setAliveState();
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

            player.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1.0F,
                    attempt.ignoresArmor(this, damaged) ? 1.5F : 2F));

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

                if (hotbarObjectGroup != null) {
                    for (HotbarObject hotbarObject : hotbarObjectGroup.getHotbarObjectMap().values()) {
                        if (hotbarObject instanceof FrozenBullets frozenBullets) {
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
                        } else if (hotbarObject instanceof FlamingBullets flamingBullets) {
                            damaged.setFireTicks(flamingBullets.getDuration());
                        }
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

        if (hotbarObjectGroup != null) {
            for (Integer slot : hotbarObjectGroup.getHotbarObjectMap().keySet()) {
                HotbarObject hotbarObject = hotbarObjectGroup.getHotbarObject(slot);
                if (hotbarObject instanceof Perk<?, ?, ?, ?> perk) {
                    perk.activate();
                }
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

        if (hotbarObjectGroup != null) {
            if (remove) {
                for (Integer slot : new HashSet<>(hotbarObjectGroup.getHotbarObjectMap().keySet())) {
                    hotbarObjectGroup.remove(slot, true);
                }
            } else {
                for (Integer slot : hotbarObjectGroup.getHotbarObjectMap().keySet()) {
                    HotbarObject hotbarObject = hotbarObjectGroup.getHotbarObject(slot);
                    if (hotbarObject instanceof Perk<?, ?, ?, ?> perk) {
                        perk.deactivate();
                    }
                }
            }
        }
    }

    /**
     * Adds all default zombies player tasks
     */
    protected void addTasks() {
        tasks.add(new WindowRepairTask(this));
        tasks.add(new ReviveTask(this));
        tasks.add(new BoundsCheckTask(this));
    }

    public void setKnockedState() {
        Player player = getPlayer();

        if (player != null) {
            ArenaApi.getInstance().applyDefaultCondition(player);
            //noinspection ConstantConditions
            player.getEquipment().setArmorContents(new ItemStack[4]);
            player.setWalkSpeed(0);
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128, false,
                    false, false));
            player.setInvulnerable(true);
            player.setGameMode(GameMode.ADVENTURE);
            getArena().getHiddenPlayers().add(player);
        }
    }

    public void setAliveState() {
        Player player = getPlayer();

        if (player != null) {
            ArenaApi.getInstance().applyDefaultCondition(player);
            //noinspection ConstantConditions
            player.getEquipment().setArmorContents(equipment);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, 2, false,
                    false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, false, false, false));
            player.setInvulnerable(false);
            player.setGameMode(GameMode.ADVENTURE);
            arena.getHiddenPlayers().remove(player);
        }
    }

    public void setDeadState() {
        Player player = getPlayer();

        if (player != null) {
            ArenaApi.getInstance().applyDefaultCondition(player);
            player.setAllowFlight(true);
            player.setCollidable(false);
            player.setGameMode(GameMode.ADVENTURE);
            arena.getHiddenPlayers().add(player);
        }
    }
}
