package io.github.zap.zombies.game;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.event.EventHandler;
import io.github.zap.arenaapi.event.ProxyEvent;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.corpse.Corpse;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.equipment.EquipmentManager;
import io.github.zap.zombies.game.data.map.*;
import io.github.zap.zombies.game.data.map.shop.DoorData;
import io.github.zap.zombies.game.data.map.shop.ShopData;
import io.github.zap.zombies.game.data.map.shop.ShopManager;
import io.github.zap.zombies.game.data.powerups.DamageModificationPowerUpData;
import io.github.zap.zombies.game.hotbar.ZombiesHotbarManager;
import io.github.zap.zombies.game.powerups.DamageModificationPowerUp;
import io.github.zap.zombies.game.powerups.PowerUp;
import io.github.zap.zombies.game.powerups.PowerUpBossBar;
import io.github.zap.zombies.game.powerups.PowerUpState;
import io.github.zap.zombies.game.powerups.events.PowerUpChangedEventArgs;
import io.github.zap.zombies.game.powerups.managers.PowerUpManager;
import io.github.zap.zombies.game.powerups.spawnrules.PowerUpSpawnRule;
import io.github.zap.zombies.game.scoreboards.GameScoreboard;
import io.github.zap.zombies.game.shop.LuckyChest;
import io.github.zap.zombies.game.shop.Shop;
import io.github.zap.zombies.game.shop.ShopEventArgs;
import io.github.zap.zombies.game.shop.ShopType;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitWorld;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDespawnEvent;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Encapsulates an active Zombies game and handles most related logic.
 */
public class ZombiesArena extends ManagingArena<ZombiesArena, ZombiesPlayer> implements Listener {
    public interface Spawner {
        /**
         * Spawns a wave.
         * @param waveData The wave data to spawn
         */
        void spawnWave(WaveData waveData);

        /**
         * Spawns mobs, outside of a wave.
         * @param mobs The
         */
        List<ActiveMob> spawnMobs(List<SpawnEntryData> mobs, SpawnMethod method, double slaSquared, boolean randomize);

        /**
         * Spawns the mob at the given vector. Will not perform SLA or any validity checks whatsoever.
         * @param mobType The type of mob to spawn
         * @param vector The vector to spawn it at
         */
        ActiveMob spawnAt(String mobType, Vector vector);
    }

    /**
     * General interface for an implementation that handles damaging all entities.
     */
    public interface DamageHandler {
        /**
         * Damages an entity.
         * @param target The ActiveMob to damage
         */
        void damageEntity(@NotNull Damager comesFrom, @NotNull DamageAttempt with, @NotNull Mob target);
    }

    /**
     * Basic spawner implementation.
     */
    @RequiredArgsConstructor
    public class BasicSpawner implements Spawner {
        @Value
        private class SpawnContext {
            SpawnpointData spawnpointData;
            WindowData window;
        }

        @Override
        public void spawnWave(WaveData wave) {
            spawnMobInternal(wave.getSpawnEntries(), wave.getMethod(), wave.getSlaSquared(), wave.isRandomizeSpawnpoints())
                .forEach(x -> x.getEntity().setMetadata(Zombies.SPAWNINFO_WAVE_METADATA_NAME, new FixedMetadataValue(Zombies.getInstance(), wave)));
        }

        private List<ActiveMob> spawnMobInternal(List<SpawnEntryData> mobs, SpawnMethod method, double slaSquared, boolean randomize) {
            List<SpawnContext> spawnpoints = filterSpawnpoints(mobs, method, slaSquared);
            List<ActiveMob> spawnedEntities = new ArrayList<>();

            if(spawnpoints.size() == 0) {
                Zombies.warning("There are no available spawnpoints for this mob set. This likely indicates an error " +
                        "in map configuration.");
                return Collections.emptyList();
            }

            if(randomize) {
                Collections.shuffle(spawnpoints); //shuffle small candidate set of spawnpoints
            }

            for(SpawnEntryData spawnEntryData : mobs) {
                int amt = spawnEntryData.getMobCount();

                outer:
                while(true) {
                    int startAmt = amt;
                    for(SpawnContext spawnContext : spawnpoints) {
                        if(method == SpawnMethod.IGNORE_SPAWNRULE || spawnContext.spawnpointData.canSpawn(spawnEntryData.getMobName(), map)) {
                            spawnMob(spawnEntryData.getMobName(), spawnContext.spawnpointData.getSpawn(), entity -> {
                                Entity bukkitEntity = entity.getEntity().getBukkitEntity();
                                bukkitEntity.setMetadata(Zombies.ARENA_METADATA_NAME, new FixedMetadataValue(Zombies.getInstance(), ZombiesArena.this));
                                bukkitEntity.setMetadata(Zombies.WINDOW_METADATA_NAME, new FixedMetadataValue(Zombies.getInstance(), spawnContext.window));

                                entity.getEntity().setMetadata(Zombies.SPAWNINFO_ENTRY_METADATA_NAME, new FixedMetadataValue(Zombies.getInstance(), spawnEntryData));
                                spawnedEntities.add(entity);
                            });

                            if(--amt == 0) {
                                break outer;
                            }
                        }
                    }

                    if(startAmt == amt) { //make sure we managed to spawn at least one mob
                        Zombies.warning("Unable to find a valid spawnpoint for SpawnEntryData.");
                        break;
                    }
                }
            }

            return spawnedEntities;
        }

        @Override
        public List<ActiveMob> spawnMobs(List<SpawnEntryData> mobs, SpawnMethod method, double slaSquared, boolean randomize) {
            List<ActiveMob> spawned = spawnMobInternal(mobs, method, slaSquared, randomize);
            zombiesLeft += spawned.size();
            return spawned;
        }

        @Override
        public ActiveMob spawnAt(String mobType, Vector vector) {
            ActiveMob spawned = spawnMob(mobType, vector, entity -> {
                Entity bukkitEntity = entity.getEntity().getBukkitEntity();
                bukkitEntity.setMetadata(Zombies.ARENA_METADATA_NAME, new FixedMetadataValue(Zombies.getInstance(), ZombiesArena.this));
                bukkitEntity.setMetadata(Zombies.WINDOW_METADATA_NAME, new FixedMetadataValue(Zombies.getInstance(), null));

                entity.getEntity().setMetadata(Zombies.SPAWNINFO_ENTRY_METADATA_NAME, new FixedMetadataValue(Zombies.getInstance(), new SpawnEntryData(mobType, 1)));
            });

            if(spawned != null) {
                zombiesLeft++;
            }

            return spawned;
        }

        /**
         * Spawns the mob at the specified vector.
         * @param mobName The name of the MythicMob to spawn
         * @param at The location to spawn the mob at, in this arena's world
         * @param postSpawn The consumer to be called after the entity spawns (if it does). Useful for applying metadata.
         * @return Whether or not the mob was successfully spawned
         */
        private ActiveMob spawnMob(String mobName, Vector at, Consumer<ActiveMob> postSpawn) {
            MythicMob mob = MythicMobs.inst().getMobManager().getMythicMob(mobName);

            if(mob != null) {
                ActiveMob activeMob = mob.spawn(new AbstractLocation(new BukkitWorld(world), at.getX() + 0.5, at.getY(),
                        at.getZ() + 0.5), map.getMobSpawnLevel());

                if(activeMob != null) {
                    mobs.add(activeMob.getUniqueId());
                    postSpawn.accept(activeMob);
                    return activeMob;
                }
                else {
                    Zombies.warning(String.format("An error occurred while trying to spawn mob of type '%s'.", mobName));
                }
            }
            else {
                Zombies.warning(String.format("Mob type '%s' is not known.", mobName));
            }

            return null;
        }

        /**
         * This rather interesting function filters only the spawnpoints that matter for this particular operation.
         * Spawnpoints that are in closed rooms, spawnpoints that can't spawn any of the mobs in this
         * wave, and spawnpoints that are out of range are all filtered out, leaving only the ones that may be needed.
         * We do all this filtering to minimize the set of spawnpoints that we have to shuffle/iterate through later
         * @param mobs The mobs to spawn
         * @param method The SpawnMethod to use
         * @param slaSquared The distance from the player that all mobs must spawn, squared
         * @return A list of SpawnpointData objects that have been properly filtered.
         */
        private List<SpawnContext> filterSpawnpoints(List<SpawnEntryData> mobs, SpawnMethod method, double slaSquared) {
            List<SpawnContext> filtered = new ArrayList<>();

            for(RoomData room : map.getRooms()) { //iterate rooms
                if(room.isSpawn() || method == SpawnMethod.FORCE || room.getOpenProperty().getValue(ZombiesArena.this)) {
                    for(SpawnpointData sample : room.getSpawnpoints()) { //check room spawnpoints first
                        if(canSpawnAny(sample, mobs, method, slaSquared)) {
                            filtered.add(new SpawnContext(sample, null));
                        }
                    }

                    for(WindowData window : room.getWindows()) {
                        for(SpawnpointData sample : window.getSpawnpoints()) {
                            if(canSpawnAny(sample, mobs, method, slaSquared)) {
                                filtered.add(new SpawnContext(sample, window));
                            }
                        }
                    }
                }
            }

            return filtered;
        }

        private boolean canSpawnAny(SpawnpointData spawnpoint, List<SpawnEntryData> entry, SpawnMethod method, double slaSquared) {
            if(method == SpawnMethod.IGNORE_SPAWNRULE) {
                return inRange(spawnpoint, slaSquared);
            }
            else {
                for(SpawnEntryData data : entry) {
                    if(spawnpoint.canSpawn(data.getMobName(), map)) {
                        return method != SpawnMethod.RANGED || inRange(spawnpoint, slaSquared);
                    }
                }
            }

            return false;
        }

        private boolean inRange(SpawnpointData target, double slaSquared) {
            for(ZombiesPlayer player : getPlayerMap().values()) {
                if(player.getPlayer().getLocation().toVector().distanceSquared(target.getSpawn()) <= slaSquared) {
                    return true;
                }
            }

            return false;
        }
    }

    public class BasicDamageHandler implements DamageHandler {
        @Override
        public void damageEntity(@NotNull Damager damager, @NotNull DamageAttempt with, @NotNull Mob target) {
            if (mobs.contains(target.getUniqueId())) {
                target.playEffect(EntityEffect.HURT);

                double deltaHealth = inflictDamage(target, with.damageAmount(damager, target), with.ignoresArmor(damager, target));
                target.setVelocity(target.getVelocity().add(with.directionVector(damager, target).clone().multiply(with.knockbackFactor(damager, target))));

                damager.onDealsDamage(with, target, deltaHealth);
            }
        }

        private double inflictDamage(Mob mob, double damage, boolean ignoreArmor) {
            boolean instaKill = false;

            for(PowerUp powerup : getPowerUps()) {
                if(powerup instanceof DamageModificationPowerUp && powerup.getState() == PowerUpState.ACTIVATED) {
                    var data = (DamageModificationPowerUpData) powerup.getData();
                    if(data.isInstaKill()) {
                        instaKill = true;
                        break;
                    }

                    damage = damage * data.getMultiplier() + data.getAdditionalDamage();
                }
            }

            double before = mob.getHealth();

            Optional<ActiveMob> activeMob = MythicMobs.inst().getMobManager().getActiveMob(mob.getUniqueId());
            boolean resistInstakill = false;
            if(activeMob.isPresent()) {
                resistInstakill = activeMob.get().getType().getConfig().getBoolean("ResistInstakill", false);
            }

            if(instaKill && !resistInstakill) {
                mob.setHealth(0);
            } else if(ignoreArmor) {
                mob.setHealth(Math.max(mob.getHealth() - damage, 0));
            } else {
                mob.damage(damage);
            }

            mob.playEffect(EntityEffect.HURT);
            return before - mob.getHealth();
        }
    }

    @Getter
    private final MapData map;

    @Getter
    private final EquipmentManager equipmentManager;

    @Getter
    private final PowerUpManager powerUpManager;

    @Getter
    private final ShopManager shopManager;

    @Getter
    protected ZombiesArenaState state = ZombiesArenaState.PREGAME;

    @Getter
    private final long emptyTimeout;

    @Getter
    private final Spawner spawner;

    @Getter
    private final DamageHandler damageHandler;

    @Getter
    private final Set<UUID> mobs = new HashSet<>();

    @Getter
    private final List<Shop<?>> shops = new ArrayList<>();

    @Getter
    private final Map<ShopType, List<Shop<?>>> shopMap = new HashMap<>();

    @Getter
    private final Map<ShopType, Event<ShopEventArgs>> shopEvents = new HashMap<>();

    @Getter
    private final String corpseTeamName = UUID.randomUUID().toString().substring(0, 16);

    @Getter
    private final Set<Corpse> corpses = new HashSet<>();

    @Getter
    private final Set<Corpse> availableCorpses = new HashSet<>();

    @Getter
    private final GameScoreboard gameScoreboard;

    @Getter
    private final Event<MythicMobDeathEvent> mythicMobDeathEvent;

    /**
     * Indicate when the game start using System.currentTimeMillis()
     * return -1 if the game hasn't start
     */
    @Getter
    private long startTimeStamp = -1;

    /**
     * Indicate when the game end using System.currentTimeMillis()
     * return -1 if the game hasn't end
     */
    @Getter
    private long endTimeStamp = -1;

    private final List<Integer> waveSpawnerTasks = new ArrayList<>();
    private int timeoutTaskId = -1;

    private BukkitTask gameEndTimeoutTask;

    @Getter
    private final Set<ImmutablePair<PowerUpSpawnRule<?>, String>> powerUpSpawnRules = new HashSet<>();

    // Contains both active and has not been picked up
    @Getter
    private final Set<PowerUp> powerUps = new HashSet<>();

    @Getter
    private final Event<PowerUpChangedEventArgs> powerUpChangedEvent = new Event<>();

    @Getter
    private final PowerUpBossBar powerUpBossBar = new PowerUpBossBar(this, 5);

    @Getter
    private int zombiesLeft;

    /**
     * Creates a new ZombiesArena with the specified map, world, and timeout.
     *
     * @param map          The map to use
     * @param world        The world to use
     * @param emptyTimeout The time it will take the arena to close, if it is empty and in the pregame state
     */
    public ZombiesArena(ZombiesArenaManager manager, World world, MapData map, long emptyTimeout) {
        super(Zombies.getInstance(), manager, world, ZombiesPlayer::new);

        this.map = map;
        this.equipmentManager = manager.getEquipmentManager();
        this.powerUpManager = manager.getPowerUpManager();
        this.shopManager = manager.getShopManager();
        this.emptyTimeout = emptyTimeout;
        this.spawner = new BasicSpawner();
        this.damageHandler = new BasicDamageHandler();
        this.gameScoreboard = new GameScoreboard(this);
        gameScoreboard.initialize();

        mythicMobDeathEvent = new ProxyEvent<>(Zombies.getInstance(), this,
                MythicMobDeathEvent.class);
        Event<MythicMobDespawnEvent> mythicMobDespawnEvent = new ProxyEvent<>(Zombies.getInstance(), this,
                MythicMobDespawnEvent.class);

        mythicMobDeathEvent.registerHandler(this::onMobDeath);
        mythicMobDespawnEvent.registerHandler(this::onMobDespawn);

        getPlayerJoinEvent().registerHandler(this::onPlayerJoin);
        getPlayerLeaveEvent().registerHandler(this::onPlayerLeave);
        getPlayerDropItemEvent().registerHandler(this::onPlayerDropItem);
        getPlayerSwapHandItemsEvent().registerHandler(this::onPlayerSwapHandItems);
        getPlayerDamageEvent().registerHandler(this::onPlayerDamage);
        getEntityDamageByEntityEvent().registerHandler(this::onEntityDamageByEntity);
        getPlayerDeathEvent().registerHandler(this::onPlayerDeath);
        getPlayerInteractEvent().registerHandler(this::onPlayerInteract);
        getPlayerInteractAtEntityEvent().registerHandler(this::onPlayerInteractAtEntity);
        getPlayerAnimationEvent().registerHandler(this::onPlayerAnimation);
        getPlayerToggleSneakEvent().registerHandler(this::onPlayerSneak);
        getPlayerItemHeldEvent().registerHandler(this::onPlayerItemHeld);
        getPlayerItemConsumeEvent().registerHandler(this::onPlayerItemConsume);
        getPlayerItemDamageEvent().registerHandler(this::onPlayerItemDamage);
        getPlayerAttemptPickupItemEvent().registerHandler(this::onPlayerAttemptPickupItem);
        getPlayerArmorStandManipulateEvent().registerHandler(this::onPlayerArmorStandManipulate);
        getPlayerFoodLevelChangeEvent().registerHandler(this::onPlayerFoodLevelChange);
        getInventoryClickEvent().registerHandler(this::onPlayerInventoryClick);

        PacketContainer createTeamPacketContainer = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        createTeamPacketContainer.getStrings().write(0, UUID.randomUUID().toString().substring(0, 16));
        createTeamPacketContainer.getIntegers().write(0, 0);
        createTeamPacketContainer.getStrings()
                .write(1, "never")
                .write(2, "never");

        getMap().getPowerUpSpawnRules()
                .forEach(x -> powerUpSpawnRules.add(ImmutablePair.of(getPowerUpManager().createSpawnRule(x.left, x.right, this), x.right)));
    }

    @Override
    public ZombiesArena getArena() {
        return this;
    }

    @Override
    public void dispose() {
        super.dispose(); //dispose of superclass-specific resources

        gameScoreboard.dispose(); // dispose resource related to managing game scoreboard

        //unregister tasks
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.cancelTask(timeoutTaskId);

        for(int taskId : waveSpawnerTasks) {
            scheduler.cancelTask(taskId);
        }

        if(powerUpBossBar != null) {
            powerUpBossBar.dispose();
        }

        //cleanup mappings and remove arena from manager
        Property.removeMappingsFor(this);
        manager.unloadArena(getArena());
    }

    @Override
    public boolean allowPlayers() {
        return state != ZombiesArenaState.ENDED && (state != ZombiesArenaState.STARTED || map.isAllowRejoin());
    }

    @Override
    public boolean allowPlayerJoin(List<Player> players) {
        return (state == ZombiesArenaState.PREGAME || state == ZombiesArenaState.COUNTDOWN) &&
                getOnlineCount() + players.size() <= map.getMaximumCapacity();
    }

    public boolean allowPlayerRejoin(List<ZombiesPlayer> players) {
        return state == ZombiesArenaState.STARTED && map.isAllowRejoin();
    }

    private void onPlayerJoin(PlayerListArgs args) {
        if(state == ZombiesArenaState.PREGAME && getOnlineCount() >= map.getMinimumCapacity()) {
            state = ZombiesArenaState.COUNTDOWN;
        }

        for(Player player : args.getPlayers()) {
            player.teleport(WorldUtils.locationFrom(world, map.getSpawn()));

            if(state == ZombiesArenaState.PREGAME) {
                player.sendTitle(ChatColor.YELLOW + "ZOMBIES", "Test version!", 0, 60, 20);
            }
        }

        resetTimeout(); //if arena was in timeout state, reset that
    }

    private void onPlayerLeave(ManagedPlayerListArgs args) {
        for(ZombiesPlayer player : args.getPlayers()) { //quit has already been called for these players
            if(!map.isAllowRejoin()) {
                super.removePlayer(player);
            }

            player.getPlayer().teleport(getManager().getHubLocation());
        }

        switch (state) {
            case PREGAME:
                removePlayers(args.getPlayers());

                if (getOnlineCount() == 0) {
                    startTimeout();
                }
                break;
            case COUNTDOWN:
                removePlayers(args.getPlayers());

                if (getOnlineCount() == 0) {
                    startTimeout();
                }
                if (getOnlineCount() < map.getMinimumCapacity()) {
                    state = ZombiesArenaState.PREGAME;
                }
                break;
            case STARTED:
                if (getOnlineCount() == 0) {
                    state = ZombiesArenaState.ENDED;
                    dispose(); //shut everything down immediately if everyone leaves mid-game
                }
                break;
        }
    }

    private void onMobDeath(MythicMobDeathEvent args) {
        if(mobs.remove(args.getEntity().getUniqueId())) {
            zombiesLeft--;
            if(getZombiesLeft() == 0 && state == ZombiesArenaState.STARTED){
                doRound();
            }
        }
    }

    private void onMobDespawn(MythicMobDespawnEvent args) {
        onMobDeath(new MythicMobDeathEvent(args.getMob(), null, null));
    }

    private void onPlayerDamage(ProxyArgs<EntityDamageEvent> args) {
        ZombiesPlayer managedPlayer = args.getManagedPlayer();
        if (managedPlayer != null && !managedPlayer.isAlive()) {
            args.getEvent().setCancelled(true);
        }
    }

    private void onEntityDamageByEntity(ProxyArgs<EntityDamageByEntityEvent> args) {
        Entity damager = args.getEvent().getDamager();
        ZombiesPlayer damagingPlayer = getPlayerMap().get(damager.getUniqueId());
        if(damagingPlayer != null && !damagingPlayer.isAlive()) {
            args.getEvent().setCancelled(true);
        }
    }

    private void onPlayerDeath(ProxyArgs<PlayerDeathEvent> args) {
        args.getEvent().setCancelled(true); //cancel death event

        ZombiesPlayer managedPlayer = args.getManagedPlayer();
        managedPlayer.knock();

        for(ZombiesPlayer player : getPlayerMap().values()) {
            if(player.isAlive()) {
                return; //return if there are any players still alive
            }
        }

        for(ZombiesPlayer player : getPlayerMap().values()) {
            player.kill();
        }

        doLoss(); //there are no players alive, so end the game
    }

    private void onPlayerInteract(ProxyArgs<PlayerInteractEvent> args) {
        PlayerInteractEvent event = args.getEvent();
        ZombiesPlayer player = args.getManagedPlayer();

        if(event.getHand() == EquipmentSlot.HAND && player.isAlive()) {
            boolean noPurchases = true;
            for (Shop<?> shop : shops) {
                if (shop.purchase(args)) {
                    noPurchases = false;
                    break;
                }
            }

            if (noPurchases) {
                player.getHotbarManager().click(event.getAction());
            }
        }
    }

    private void onPlayerSwapHandItems(ProxyArgs<PlayerSwapHandItemsEvent> args) {
        args.getEvent().setCancelled(true);
    }

    private void onPlayerInteractAtEntity(ProxyArgs<PlayerInteractAtEntityEvent> args) {
        PlayerInteractAtEntityEvent event = args.getEvent();
        ZombiesPlayer player = args.getManagedPlayer();

        if (event.getHand() == EquipmentSlot.HAND && player.isAlive()) {
            boolean noPurchases = true;
            for (Shop<?> shop : shops) {
                if (shop.purchase(args)) {
                    noPurchases = false;
                    break;
                }
            }
            if (noPurchases) {
                player.getHotbarManager().click(Action.RIGHT_CLICK_BLOCK);
            }
        }
    }

    private void onPlayerDropItem(ProxyArgs<PlayerDropItemEvent> args) {
        PlayerDropItemEvent event = args.getEvent();
        ItemStack stack = event.getPlayer().getInventory().getItem(EquipmentSlot.HAND);

        if(stack == null || stack.getType() == Material.AIR) {
            event.setCancelled(true);
        }
        else {
            event.getItemDrop().remove();
            stack.setAmount(stack.getAmount() + 1);
            event.getPlayer().updateInventory();
        }
    }

    private void onPlayerAnimation(ProxyArgs<PlayerAnimationEvent> args) {
        PlayerAnimationEvent event = args.getEvent();
        ZombiesPlayer player = args.getManagedPlayer();

        // why does Bukkit only have one animation type?
        if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
            player.getHotbarManager().click(Action.LEFT_CLICK_BLOCK);
        }
    }

    private void onPlayerSneak(ProxyArgs<PlayerToggleSneakEvent> args) {
        PlayerToggleSneakEvent event = args.getEvent();
        ZombiesPlayer managedPlayer = args.getManagedPlayer();

        if(event.isSneaking()) {
            if (managedPlayer.isAlive()) {
                managedPlayer.activateRepair();
                managedPlayer.activateRevive();
            }
        }
        else {
            managedPlayer.disableRepair();
            managedPlayer.disableRevive();
        }
    }

    private void onPlayerItemHeld(ProxyArgs<PlayerItemHeldEvent> args) {
        PlayerItemHeldEvent event = args.getEvent();
        ZombiesPlayer managedPlayer = args.getManagedPlayer();

        managedPlayer.getHotbarManager().setSelectedSlot(event.getNewSlot());
    }

    private void onPlayerItemConsume(ProxyArgs<PlayerItemConsumeEvent> args) {
        args.getEvent().setCancelled(true); // TODO: might need to change this one day
    }

    private void onPlayerItemDamage(ProxyArgs<PlayerItemDamageEvent> args) {
        args.getEvent().setCancelled(true);
    }

    private void onPlayerAttemptPickupItem(ProxyArgs<PlayerAttemptPickupItemEvent> args) {
        args.getEvent().setCancelled(true);
    }

    private void onPlayerArmorStandManipulate(ProxyArgs<PlayerArmorStandManipulateEvent> args) {
        args.getEvent().setCancelled(true);
    }

    private void onPlayerFoodLevelChange(ProxyArgs<FoodLevelChangeEvent> args) {
        FoodLevelChangeEvent event = args.getEvent();
        event.setCancelled(true);
    }

    private void onPlayerInventoryClick(ManagedInventoryEventArgs<InventoryClickEvent> args) {
        InventoryClickEvent event = args.getEvent();
        ZombiesPlayer managedPlayer = getPlayerMap().get(event.getWhoClicked().getUniqueId());
        if (managedPlayer != null) {
            Player player = managedPlayer.getPlayer();

            if (player.getInventory().equals(event.getClickedInventory())) {
                event.setCancelled(true);
            }
        }
    }

    public void startGame() {
        if(state == ZombiesArenaState.PREGAME || state == ZombiesArenaState.COUNTDOWN) {
            loadShops();

            state = ZombiesArenaState.STARTED;
            startTimeStamp = System.currentTimeMillis();

            for(ZombiesPlayer player : getPlayerMap().values()) {
                if(player.isInGame()) {
                    player.getPlayer().sendMessage(ChatColor.YELLOW + "Started!");
                    player.setAliveState();

                    Vector spawn = map.getSpawn();
                    player.getPlayer().teleport(
                            new Location(world, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5)
                    );

                    ZombiesHotbarManager hotbarManager = player.getHotbarManager();
                    for (Map.Entry<String, Set<Integer>> hotbarObjectGroupSlot : map.getHotbarObjectGroupSlots().entrySet()) {
                        hotbarManager.addEquipmentObjectGroup(equipmentManager
                                .createEquipmentObjectGroup(hotbarObjectGroupSlot.getKey(), player.getPlayer(),
                                        hotbarObjectGroupSlot.getValue()));
                    }

                    for(String equipment : map.getDefaultEquipments()) {
                        EquipmentData<?> equipmentData = equipmentManager.getEquipmentData(map.getName(), equipment);
                        Integer slot
                                = hotbarManager.getHotbarObjectGroup(equipmentData.getEquipmentType()).getNextEmptySlot();

                        if (slot != null) {
                            hotbarManager.setHotbarObject(
                                    slot,
                                    equipmentManager.createEquipment(this, player, slot, equipmentData)
                            );
                        }
                    }

                    player.startTasks();
                }
            }

            doRound();
        }
    }

    private void doRound() {
        for(ZombiesPlayer player : getPlayerMap().values()) { //respawn players who may have died
            if(!player.isAlive()) {
                player.respawn();
            }
        }

        Property<Integer> currentRoundProperty = map.getCurrentRoundProperty();
        int currentRoundIndex = currentRoundProperty.getValue(this);

        List<RoundData> rounds = map.getRounds();
        if(currentRoundIndex < rounds.size()) {
            RoundData currentRound = rounds.get(currentRoundIndex);
            zombiesLeft = rounds.get(currentRoundIndex).getWaves().stream()
                    .flatMap(x -> x.getSpawnEntries().stream())
                    .map(SpawnEntryData::getMobCount)
                    .reduce(0, Integer::sum);

            long cumulativeDelay = 0;
            for (WaveData wave : currentRound.getWaves()) {
                cumulativeDelay += wave.getWaveLength();

                waveSpawnerTasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(Zombies.getInstance(), () -> {
                    spawner.spawnWave(wave);
                    waveSpawnerTasks.remove(0);
                }, cumulativeDelay));
            }

            currentRoundProperty.setValue(this, currentRoundIndex + 1);
            getPlayerMap().forEach((l,r) -> {
                var messageTitle = currentRound.getCustomMessage() != null && !currentRound.getCustomMessage().isEmpty() ?
                        currentRound.getCustomMessage() : ChatColor.RED + "ROUND " + (currentRoundIndex + 1);
                r.getPlayer().sendTitle(messageTitle, "");
                r.getPlayer().playSound(r.getPlayer().getLocation(), Sound.ENTITY_WITHER_SPAWN, 1, 0.5f);
            });

            if(getMap().getDisablePowerUpRound().contains(currentRoundIndex + 1)) {
                var items =getPowerUps().stream()
                        .filter(x -> x.getState() == PowerUpState.NONE || x.getState() == PowerUpState.DROPPED)
                        .collect(Collectors.toSet());
                items.forEach(PowerUp::deactivate);
            }
        }
        else {
            //game just finished, do win condition
            state = ZombiesArenaState.ENDED;
            doVictory();
        }
    }

    public void startTimeout() {
        if(timeoutTaskId == -1) {
            timeoutTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(Zombies.getInstance(), this::dispose,
                    emptyTimeout);
        }
    }

    public void resetTimeout() {
        if(timeoutTaskId != -1) {
            Bukkit.getScheduler().cancelTask(timeoutTaskId);
            timeoutTaskId = -1;
        }
    }

    /**
     * Win code here
     */
    private void doVictory() {
        state = ZombiesArenaState.ENDED;
        endTimeStamp = System.currentTimeMillis();
        var round = map.getCurrentRoundProperty().getValue(this);
        getPlayerMap().forEach((l,r) -> {
            r.getPlayer().sendTitle(ChatColor.GREEN + "You Win!", ChatColor.GRAY + "You made it to Round " + round + "!");
            r.getPlayer().sendMessage(ChatColor.YELLOW + "Zombies" + ChatColor.GRAY + " - " + ChatColor.RED + "You probably wanna change this after next beta");
        });
        waitAndDispose(200);
    }

    /**
     * Loss code here
     */
    private void doLoss() {
        state = ZombiesArenaState.ENDED;
        endTimeStamp = System.currentTimeMillis();
        var round = map.getCurrentRoundProperty().getValue(this);
        getPlayerMap().forEach((l,r) -> {
            r.getPlayer().sendTitle(ChatColor.GREEN + "Game Over!", ChatColor.GRAY + "You made it to Round " + round + "!");
            r.getPlayer().sendMessage(ChatColor.YELLOW + "Zombies" + ChatColor.GRAY + " - " + ChatColor.RED + "You probably wanna change this after next beta");
        });
        waitAndDispose(200);
    }

    /**
     * Attempts to break the given window.
     */
    public void tryBreakWindow(Entity attacker, WindowData targetWindow, int by) {
        targetWindow.getAttackingEntityProperty().setValue(this, attacker);

        int previousIndex = targetWindow.getCurrentIndexProperty().getValue(this);
        int blocksBroken = targetWindow.retractRepairState(this, by);

        for(int i = previousIndex; i > previousIndex - blocksBroken; i--) { //break the blocks
            WorldUtils.getBlockAt(world, targetWindow.getFaceVectors().get(i)).setType(Material.AIR);

            Vector center = targetWindow.getCenter();
            if(i > 0) {
                world.playSound(targetWindow.getBlockBreakSound(), center.getX(), center.getY(), center.getZ());
            }
            else {
                world.playSound(targetWindow.getWindowBreakSound(), center.getX(), center.getY(), center.getZ());
            }
        }
    }

    public boolean runAI() {
        return state == ZombiesArenaState.STARTED;
    }

    /**
     * Gets the shop event for a shop type or creates a new one
     * @param shopType The shop type
     * @return The shop type's event
     */
    public Event<ShopEventArgs> getShopEvent(ShopType shopType) {
        return shopEvents.computeIfAbsent(shopType, (ShopType type) -> new Event<>());
    }

    /**
     * Loads shops; should be called just before the game begins
     */
    private void loadShops() {
        for (ShopData shopData : map.getShops()) {
            Shop<?> shop = shopManager.createShop(this, shopData);
            shops.add(shop);
            shopMap.computeIfAbsent(shop.getShopType(), (ShopType type) -> new ArrayList<>()).add(shop);
            getShopEvent(shop.getShopType());
            shop.display();
        }

        for(DoorData doorData : map.getDoors()) {
            Shop<DoorData> shop = shopManager.createShop(this, doorData);
            shops.add(shop);
            shopMap.computeIfAbsent(shop.getShopType(), (ShopType type) -> new ArrayList<>()).add(shop);
            shop.display();
        }
        getShopEvent(ShopType.DOOR);

        Event<ShopEventArgs> chestEvent = shopEvents.get(ShopType.LUCKY_CHEST);
        if (chestEvent != null) {
            chestEvent.registerHandler(new EventHandler<>() {
                private final Random random = new Random();
                int rolls = 0;

                @Override
                public void handleEvent(ShopEventArgs args) {
                    if (++rolls == map.getRollsPerChest()) {
                        LuckyChest luckyChest = (LuckyChest) args.getShop();
                        luckyChest.toggle(false);
                        List<Shop<?>> chests = new ArrayList<>(shopMap.get(luckyChest.getShopType()));
                        chests.remove(luckyChest);

                        ((LuckyChest) chests.get(random.nextInt(chests.size()))).toggle(true);
                        // TODO: set where the chest is
                        rolls = 0;
                    }
                }
            });
        }
    }
}
