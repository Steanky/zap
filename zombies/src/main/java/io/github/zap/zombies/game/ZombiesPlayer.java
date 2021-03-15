package io.github.zap.zombies.game;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.ResourceManager;
import io.github.zap.arenaapi.game.arena.ManagedPlayer;
import io.github.zap.arenaapi.util.AttributeHelper;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.game.corpse.Corpse;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.RoomData;
import io.github.zap.zombies.game.data.map.WindowData;
import io.github.zap.zombies.game.data.powerups.EarnedGoldMultiplierPowerUpData;
import io.github.zap.zombies.game.hotbar.ZombiesHotbarManager;
import io.github.zap.zombies.game.perk.FlamingBullets;
import io.github.zap.zombies.game.perk.FrozenBullets;
import io.github.zap.zombies.game.perk.PerkType;
import io.github.zap.zombies.game.perk.ZombiesPerks;
import io.github.zap.zombies.game.powerups.EarnedGoldMultiplierPowerUp;
import io.github.zap.zombies.game.powerups.PowerUpState;
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

    @Getter
    private final ZombiesPerks perks;

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
    public ZombiesPlayer(ZombiesArena arena, Player player) {
        super(arena, player);

        this.arena = arena;
        //noinspection ConstantConditions
        this.equipment = player.getEquipment().getArmorContents();
        this.coins = arena.getMap().getStartingCoins();

        this.hotbarManager = new ZombiesHotbarManager(getPlayer());

        this.perks = new ZombiesPerks(this);

        setAliveState();

        resourceManager = new ResourceManager(arena.getPlugin());
        resourceManager.addDisposable(perks);
    }

    public void quit() {
        super.quit();

        if(super.isInGame()) {
            state = ZombiesPlayerState.DEAD;

            perks.disableAll();
            endTasks();
        }
    }

    @Override
    public void rejoin() {
        super.rejoin();

        state = ZombiesPlayerState.DEAD;
        perks.activateAll();
        setDeadState();

        //noinspection ConstantConditions
        getPlayer().getEquipment().setArmorContents(equipment);
        hotbarManager.switchProfile(ZombiesHotbarManager.DEAD_PROFILE_NAME);
    }

    @Override
    public void dispose() {
        perks.dispose();
        endTasks();

        if(corpse != null) {
            corpse.destroy();
            corpse = null;
        }

        if(isInGame()) {
            super.quit();
        }
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
                    .filter(x -> x instanceof EarnedGoldMultiplierPowerUp && x.getState() == PowerUpState.ACTIVATED)
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

            // integer overflow check
            if(Integer.MAX_VALUE - coins - amount > 0)
                coins += amount;
            else
                coins = Integer.MAX_VALUE;
        }
    }

    public void subtractCoins(int amount) {
        if(amount > 0) {
            getPlayer().sendMessage(String.format("%s-%d Gold", ChatColor.GOLD, amount));
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
            getPlayer().sendActionBar(Component.empty());
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

            Location corpseLocation = corpse.getLocation();
            for (Player player : getPlayer().getWorld().getPlayers()) {
                player.playSound(Sound.sound(
                        Key.key("minecraft:entity.player.hurt"),
                        Sound.Source.MASTER,
                        1.0F,
                        1.0F
                ), corpseLocation.getX(), corpseLocation.getY(), corpseLocation.getZ());
            }

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
                addKills(1);
            } else {
                FrozenBullets frozenBullets = (FrozenBullets) getPerks().getPerk(PerkType.FROZEN_BULLETS);
                FlamingBullets flamingBullets = (FlamingBullets) getPerks().getPerk(PerkType.FLAME_BULLETS);

                if (frozenBullets.getCurrentLevel() > 0) {
                    AttributeInstance speed = damaged.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);

                    if (speed != null && !AttributeHelper.hasModifier(speed, FROZEN_BULLETS_ATTRIBUTE_NAME)) {
                        AttributeModifier modifier = new AttributeModifier(FROZEN_BULLETS_ATTRIBUTE_NAME,
                                -1D / ((double) frozenBullets.getCurrentLevel() + 1D), AttributeModifier.Operation.ADD_SCALAR);

                        speed.addModifier(modifier);

                        arena.runTaskLater(frozenBullets.getDuration(), () -> speed.removeModifier(modifier));
                    }
                }

                if (flamingBullets.getCurrentLevel() > 0) {
                    damaged.setFireTicks(flamingBullets.getDuration());
                }
            }
        }
    }

    /**
     * Tries to find and repair a window.
     */
    private void checkForWindow() {
        MapData map = arena.getMap();

        if(targetWindow == null) { //our target window is null, so look for one to repair
            WindowData window = map.windowMatching(windowData -> !windowData.isFullyRepaired(arena)
                    && windowData.getRepairingPlayerProperty().getValue(arena) == null
                    && windowData.inRange(getPlayer().getLocation().toVector(), arena.getMap().getWindowRepairRadiusSquared()));

            if (window != null) {
                if (repairOn) {
                    targetWindow = window;
                    tryRepairWindow(targetWindow); //directly repair window; no need to perform checks
                    getPlayer().sendActionBar(Component.text());
                } else {
                    getPlayer().sendActionBar(
                            Component.text("Hold SHIFT to repair!").color(NamedTextColor.YELLOW)
                    );
                }
            } else {
                getPlayer().sendActionBar(Component.text());
            }
        }
        else { //we already have a target window - make sure it's still in range
            if (targetWindow.inRange(getPlayer().getLocation().toVector(), map.getWindowRepairRadiusSquared())
                    && repairOn && isAlive()) {
                tryRepairWindow(targetWindow);
            }
            else {
                targetWindow = null;
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
                    Block target = WorldUtils.getBlockAt(arena.getWorld(), targetWindow.getFaceVectors().get(i + 1));
                    target.setBlockData(Bukkit.createBlockData(targetWindow.getRepairedData().get(i + 1)));

                    Vector center = targetWindow.getCenter();
                    if(i < targetWindow.getVolume() - 2) {
                        arena.getWorld().playSound(targetWindow.getBlockRepairSound(), center.getX(), center.getY(), center.getZ());
                    }
                    else {
                        arena.getWorld().playSound(targetWindow.getWindowRepairSound(), center.getX(), center.getY(), center.getZ());
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
        if (isAlive()) {
            int maxDistance = arena.getMap().getReviveRadius();

            if (targetCorpse == null) {
                selectNewCorpse();
            } else if (!targetCorpse.isActive()) {
                getPlayer().sendActionBar(Component.text());
                targetCorpse = null;

                selectNewCorpse();
            } else {
                double distance = getPlayer().getLocation().toVector().distanceSquared(targetCorpse.getLocation().toVector());

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
        int maxDistance = arena.getMap().getReviveRadius();

        for (Corpse corpse : arena.getAvailableCorpses()) {
            double distance
                    = getPlayer().getLocation().toVector().distanceSquared(corpse.getLocation().toVector());
            if (distance <= maxDistance) {
                if (reviveOn) {
                    targetCorpse = corpse;
                    targetCorpse.setReviver(this);
                    targetCorpse.continueReviving();
                } else {
                    getPlayer().sendActionBar(Component.text(
                            String.format("Hold SHIFT to Revive %s!", corpse.getZombiesPlayer().getPlayer().getName())
                            ).color(NamedTextColor.YELLOW)
                    );
                }

                break;
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
            player.setInvisible(true);
            player.setGameMode(GameMode.ADVENTURE);
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
            player.setInvulnerable(false);
            player.setGameMode(GameMode.ADVENTURE);
            startTasks();
        }
    }

    public void setDeadState() {
        Player player = getPlayer();

        if(player != null) {
            ArenaApi.getInstance().applyDefaultCondition(player);
            player.setAllowFlight(true);
            player.setInvisible(true);
            player.setCollidable(false);
            player.setGameMode(GameMode.ADVENTURE);
        }
    }
}
