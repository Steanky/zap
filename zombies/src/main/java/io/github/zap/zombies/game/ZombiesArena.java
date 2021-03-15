package io.github.zap.zombies.game;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.ResourceManager;
import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.event.EventHandler;
import io.github.zap.arenaapi.event.FilteredEvent;
import io.github.zap.arenaapi.event.ProxyEvent;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.util.MetadataHelper;
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
import io.github.zap.zombies.game.equipment.melee.MeleeWeapon;
import io.github.zap.zombies.game.hotbar.ZombiesHotbarManager;
import io.github.zap.zombies.game.powerups.DamageModificationPowerUp;
import io.github.zap.zombies.game.powerups.PowerUp;
import io.github.zap.zombies.game.powerups.PowerUpBossBar;
import io.github.zap.zombies.game.powerups.PowerUpState;
import io.github.zap.zombies.game.powerups.events.PowerUpChangedEventArgs;
import io.github.zap.zombies.game.powerups.managers.PowerUpManager;
import io.github.zap.zombies.game.powerups.spawnrules.PowerUpSpawnRule;
import io.github.zap.zombies.game.scoreboards.GameScoreboard;
import io.github.zap.zombies.game.shop.*;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitWorld;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDespawnEvent;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import lombok.Getter;
import lombok.Value;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Encapsulates an active Zombies game and handles most related logic.
 */
public class ZombiesArena extends ManagingArena<ZombiesArena, ZombiesPlayer> implements Listener {
    public interface Spawner {
        /**
         * Spawns the provided SpawnEntries in this arena.
         * @param mobs The mobs to spawn
         * @param method The SpawnMethod to use
         * @param slaSquared The SLA to use
         * @param randomize Whether or not spawnpoints are randomized
         * @param updateCount Whether or not to update the total mob count
         * @return The ActiveMobs that were spawned
         */
        List<ActiveMob> spawnMobs(@NotNull List<SpawnEntryData> mobs, @NotNull SpawnMethod method, double slaSquared,
                                  boolean randomize, boolean updateCount);

        /**
         * Spawns the provided SpawnEntries in this arena. Only spawnpoints that match the provided predicate can spawn
         * mobs.
         * @param mobs The mobs to spawn
         * @param method The method to use while spawning
         * @param spawnpointPredicate The predicate used to additionally filter spawnpoints
         * @param slaSquared The value to use for SLA
         * @param randomize Whether or not spawnpoints are shuffled
         * @param updateCount Whether or not to update the total mob count
         * @return The ActiveMobs that were spawned as a result of this operation
         */
        List<ActiveMob> spawnMobs(@NotNull List<SpawnEntryData> mobs, @NotNull SpawnMethod method,
                                 @NotNull Predicate<SpawnpointData> spawnpointPredicate, double slaSquared,
                                  boolean randomize, boolean updateCount);

        /**
         * Spawns a mob at the specified vector, without performing range checks. If the mob is spawned inside of a
         * window, it will have the appropriate metadata set.
         * @param mobType The mob type to spawn
         * @param vector The vector to spawn the mob at
         * @return The mob that was spawned, or null if it failed to spawn
         */
        ActiveMob spawnMobAt(@NotNull String mobType, @NotNull Vector vector, boolean updateCount);
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
    public class BasicSpawner implements Spawner {
        @Value
        private class SpawnContext {
            SpawnpointData spawnpoint;
            WindowData window;
        }

        @Override
        public List<ActiveMob> spawnMobs(@NotNull List<SpawnEntryData> mobs, @NotNull SpawnMethod method,
                                         double slaSquared, boolean randomize, boolean updateCount) {
            return spawnMobs(mobs, method, spawnpointData -> true, slaSquared, randomize, updateCount);
        }

        @Override
        public ActiveMob spawnMobAt(@NotNull String mobType, @NotNull Vector vector, boolean updateCount) {
            ActiveMob spawned = spawnMob(mobType, vector);

            if(spawned != null) {
                if(updateCount) {
                    zombiesLeft++;
                }

                RoomData roomIn = map.roomAt(vector);
                if(roomIn != null) {
                    for(WindowData windowData : roomIn.getWindows()) {
                        if(windowData.playerInside(vector)) {
                            MetadataHelper.setMetadataFor(spawned.getEntity().getBukkitEntity(),
                                    Zombies.WINDOW_METADATA_NAME, Zombies.getInstance(), windowData);
                            break;
                        }
                    }
                }
            }

            return spawned;
        }

        @Override
        public List<ActiveMob> spawnMobs(@NotNull List<SpawnEntryData> mobs, @NotNull SpawnMethod method,
                                         @NotNull Predicate<SpawnpointData> filter, double slaSquared,
                                         boolean randomize, boolean updateCount) {
            List<SpawnContext> spawns = filterSpawnpoints(mobs, method, filter, slaSquared);
            List<ActiveMob> spawnedEntities = new ArrayList<>();

            if(spawns.size() == 0) {
                Zombies.warning("There are no available spawnpoints for this mob set. This likely indicates an error " +
                        "in map configuration.");
                return Collections.emptyList();
            }

            if(randomize) {
                Collections.shuffle(spawns); //shuffle small candidate set of spawnpoints
            }

            for(SpawnEntryData spawnEntryData : mobs) {
                int amt = spawnEntryData.getMobCount();

                outer:
                while(true) {
                    int startAmt = amt;
                    for(SpawnContext context : spawns) {
                        if(method == SpawnMethod.IGNORE_SPAWNRULE || context.spawnpoint.canSpawn(spawnEntryData.getMobName(), map)) {
                            ActiveMob mob = spawnMob(spawnEntryData.getMobName(), context.spawnpoint.getSpawn());

                            if(mob != null) {
                                MetadataHelper.setMetadataFor(mob.getEntity().getBukkitEntity(),
                                        Zombies.WINDOW_METADATA_NAME, Zombies.getInstance(), context.window);

                                if(updateCount) {
                                    zombiesLeft++;
                                }
                            }
                            else {
                                //mob failed to spawn; if we're part of a wave, reduce the amt of zombies
                                if(!updateCount) {
                                    zombiesLeft--;
                                    checkNextRound();
                                }

                                Zombies.warning("Mob failed to spawn!");
                            }

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

        private ActiveMob spawnMob(String mobName, Vector blockPosition) {
            MythicMob mob = MythicMobs.inst().getMobManager().getMythicMob(mobName);

            if(mob != null) {
                ActiveMob activeMob = mob.spawn(new AbstractLocation(new BukkitWorld(world), blockPosition.getX() +
                        0.5, blockPosition.getY(), blockPosition.getZ() + 0.5), map.getMobSpawnLevel());

                if(activeMob != null) {
                    getEntitySet().add(activeMob.getUniqueId());
                    MetadataHelper.setMetadataFor(activeMob.getEntity().getBukkitEntity(), Zombies.ARENA_METADATA_NAME,
                            Zombies.getInstance(), ZombiesArena.this);
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

        private List<SpawnContext> filterSpawnpoints(List<SpawnEntryData> mobs, SpawnMethod method,
                                                     Predicate<SpawnpointData> filter, double slaSquared) {
            List<SpawnContext> filtered = new ArrayList<>();

            for(RoomData room : map.getRooms()) { //iterate rooms
                if(room.isSpawn() || method == SpawnMethod.FORCE || room.getOpenProperty().getValue(ZombiesArena.this)) {
                    //add all valid spawnpoints in the room
                    addValidContext(filtered, room.getSpawnpoints(), mobs, method, filter, slaSquared, null);

                    for(WindowData window : room.getWindows()) { //check window spawnpoints next
                        addValidContext(filtered, window.getSpawnpoints(), mobs, method, filter, slaSquared, window);
                    }
                }
            }

            return filtered;
        }

        private void addValidContext(List<SpawnContext> addTo, List<SpawnpointData> spawnpoints, List<SpawnEntryData> mobs,
                                     SpawnMethod method, Predicate<SpawnpointData> filter,
                                     double slaSquared, WindowData window) {
            for(SpawnpointData spawnpointData : spawnpoints) {
                if(filter.test(spawnpointData) && canSpawnAny(spawnpointData, mobs, method, slaSquared)) {
                    addTo.add(new SpawnContext(spawnpointData, window));
                }
            }
        }

        private boolean canSpawnAny(SpawnpointData spawnpoint, List<SpawnEntryData> entry, SpawnMethod method, double slaSquared) {
            if(method == SpawnMethod.IGNORE_SPAWNRULE) {
                return checkSLA(spawnpoint, slaSquared);
            }
            else {
                for(SpawnEntryData data : entry) {
                    if(spawnpoint.canSpawn(data.getMobName(), map)) {
                        return method != SpawnMethod.RANGED || checkSLA(spawnpoint, slaSquared);
                    }
                }
            }

            return false;
        }

        private boolean checkSLA(SpawnpointData target, double slaSquared) {
            for(ZombiesPlayer player : getPlayerMap().values()) {
                Player bukkitPlayer = player.getPlayer();

                if(bukkitPlayer != null) {
                    if(bukkitPlayer.getLocation().toVector().distanceSquared(target.getSpawn()) <= slaSquared) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public class BasicDamageHandler implements DamageHandler {
        @Override
        public void damageEntity(@NotNull Damager damager, @NotNull DamageAttempt with, @NotNull Mob target) {
            if (hasEntity(target.getUniqueId())) {
                target.playEffect(EntityEffect.HURT);

                double deltaHealth = inflictDamage(target, with.damageAmount(damager, target), with.ignoresArmor(damager, target));
                Vector resultingVelocity = target.getVelocity().add(with.directionVector(damager, target)
                        .multiply(with.knockbackFactor(damager, target)));

                try {
                    target.setVelocity(resultingVelocity);
                }
                catch (IllegalArgumentException ignored) {
                    Zombies.warning("Attempted to set velocity for entity " + target.getUniqueId() + " to a vector " +
                            "with a non-finite value " + resultingVelocity.toString());
                }

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
                mob.setHealth(Math.max(mob.getHealth() - damage, 0D));
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
    private final DamageHandler damageHandler;

    @Getter
    private final Spawner spawner;

    @Getter
    protected ZombiesArenaState state = ZombiesArenaState.PREGAME;

    @Getter
    private final long emptyTimeout;

    @Getter
    private final List<Shop<?>> shops = new ArrayList<>();

    @Getter
    private final Map<ShopType, List<Shop<?>>> shopMap = new HashMap<>();

    @Getter
    private final Map<ShopType, Event<ShopEventArgs>> shopEvents = new HashMap<>();

    @Getter
    private String luckyChestRoom;

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

    @Getter
    private final Event<MythicMobDespawnEvent> mythicMobDespawnEvent;

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

    @Getter
    private final Set<Pair<PowerUpSpawnRule<?>, String>> powerUpSpawnRules = new HashSet<>();

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
        super(Zombies.getInstance(), manager, world, ZombiesPlayer::new, emptyTimeout);

        this.map = map;
        this.equipmentManager = manager.getEquipmentManager();
        this.powerUpManager = manager.getPowerUpManager();
        this.shopManager = manager.getShopManager();
        this.emptyTimeout = emptyTimeout;
        this.spawner = new BasicSpawner();
        this.damageHandler = new BasicDamageHandler();
        this.gameScoreboard = new GameScoreboard(this);
        gameScoreboard.initialize();

        mythicMobDeathEvent = new FilteredEvent<>(new ProxyEvent<>(Zombies.getInstance(), MythicMobDeathEvent.class),
                event -> event.getEntity() != null && hasEntity(event.getEntity().getUniqueId()));

        mythicMobDespawnEvent = new FilteredEvent<>(new ProxyEvent<>(Zombies.getInstance(),
                MythicMobDespawnEvent.class), event -> event.getEntity() != null && hasEntity(event.getEntity().getUniqueId()));

        registerArenaEvents();
        registerDisposables();

        PacketContainer createTeamPacketContainer = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        createTeamPacketContainer.getStrings().write(0, UUID.randomUUID().toString().substring(0, 16));
        createTeamPacketContainer.getIntegers().write(0, 0);
        createTeamPacketContainer.getStrings()
                .write(1, "never")
                .write(2, "never");

        getMap().getPowerUpSpawnRules()
                .forEach(x -> powerUpSpawnRules.add(Pair.of(getPowerUpManager().createSpawnRule(x.left, x.right, this), x.right)));

        Bukkit.getServer().getPluginManager().registerEvents(this, Zombies.getInstance());
    }

    private void registerArenaEvents() {
        getPlayerJoinEvent().registerHandler(this::onPlayerJoin);
        getPlayerLeaveEvent().registerHandler(this::onPlayerLeave);

        getProxyFor(EntityDeathEvent.class).registerHandler(this::onMobDeath);
        getProxyFor(PlayerDropItemEvent.class).registerHandler(this::onPlayerDropItem);
        getProxyFor(PlayerMoveEvent.class).registerHandler(this::onPlayerMove);
        getProxyFor(PlayerSwapHandItemsEvent.class).registerHandler(this::onPlayerSwapHandItems);
        getProxyFor(EntityDamageEvent.class).registerHandler(this::onEntityDamaged);
        getProxyFor(EntityDamageByEntityEvent.class).registerHandler(this::onEntityDamageByEntity);
        getProxyFor(PlayerDeathEvent.class).registerHandler(this::onPlayerDeath);
        getProxyFor(PlayerInteractEvent.class).registerHandler(this::onPlayerInteract);
        getProxyFor(PlayerInteractAtEntityEvent.class).registerHandler(this::onPlayerInteractAtEntity);
        getProxyFor(PlayerAnimationEvent.class).registerHandler(this::onPlayerAnimation);
        getProxyFor(PlayerToggleSneakEvent.class).registerHandler(this::onPlayerToggleSneak);
        getProxyFor(PlayerItemHeldEvent.class).registerHandler(this::onPlayerItemHeld);
        getProxyFor(PlayerItemConsumeEvent.class).registerHandler(this::onPlayerItemConsume);
        getProxyFor(PlayerItemDamageEvent.class).registerHandler(this::onPlayerItemDamage);
        getProxyFor(PlayerAttemptPickupItemEvent.class).registerHandler(this::onPlayerAttemptPickupItem);
        getProxyFor(PlayerArmorStandManipulateEvent.class).registerHandler(this::onPlayerArmorStandManipulate);
        getProxyFor(FoodLevelChangeEvent.class).registerHandler(this::onFoodLevelChange);
        getProxyFor(InventoryClickEvent.class).registerHandler(this::onPlayerInventoryClick);
    }

    private void registerDisposables() {
        ResourceManager resourceManager = getResourceManager();

        resourceManager.addDisposable(gameScoreboard);
        resourceManager.addDisposable(powerUpBossBar);
    }

    @Override
    public ZombiesArena getArena() {
        return this;
    }

    @Override
    public void dispose() {
        super.dispose(); //dispose of superclass-specific resources

        //cleanup mappings and remove arena from manager
        Property.removeMappingsFor(this);
        manager.unloadArena(getArena());

        HandlerList.unregisterAll(this);
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
    }

    private void onPlayerLeave(ManagedPlayerListArgs args) {
        for(ZombiesPlayer player : args.getPlayers()) { //quit has already been called for these players
            if(!map.isAllowRejoin()) {
                super.removePlayer(player);
            }
        }

        switch (state) {
            case PREGAME:
                removePlayers(args.getPlayers());
                break;
            case COUNTDOWN:
                removePlayers(args.getPlayers());

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

    private void onMobDeath(ProxyArgs<EntityDeathEvent> args) {
        if(state == ZombiesArenaState.STARTED) {
            if(getEntitySet().remove(args.getManagedEntity())) {
                zombiesLeft--;
            }

            checkNextRound();
        }
    }

    private void checkNextRound() {
        if(zombiesLeft == 0 && state == ZombiesArenaState.STARTED){
            doRound();
        }
    }

    private void onEntityDamaged(ProxyArgs<EntityDamageEvent> args) {
        EntityDamageEvent event = args.getEvent();
        ZombiesPlayer managedPlayer = args.getManagedPlayer();

        if (managedPlayer != null) {
            Player player = managedPlayer.getPlayer();
            ZombiesPlayerState state = managedPlayer.getState();

            if(player != null && state == ZombiesPlayerState.ALIVE) {
                if(player.getHealth() <= event.getFinalDamage()) {
                    Location location = player.getLocation();

                    for (double y = location.getY(); y >= 0D; y--){
                        location.setY(y);
                        Block block = player.getWorld().getBlockAt(location);
                        if (!block.getType().isAir()) {
                            player.teleport(location.add(0, block.getBoundingBox().getHeight(), 0));
                            break;
                        }
                    }
                }
            }
            else if(state == ZombiesPlayerState.DEAD) {
                event.setCancelled(true);
            }
        }
    }

    @org.bukkit.event.EventHandler
    private void onNonManagedEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (entity.getWorld().equals(world) && entity instanceof ItemFrame) {
            event.setCancelled(true);
        }
    }

    private void onEntityDamageByEntity(ProxyArgs<EntityDamageByEntityEvent> args) {
        EntityDamageByEntityEvent event = args.getEvent();
        Entity entity = event.getEntity(), damager = event.getDamager();
        ZombiesPlayer damagingPlayer = getPlayerMap().get(damager.getUniqueId());

        if (damagingPlayer != null && entity instanceof Mob) {
            Mob mob = (Mob) entity;

            if(!damagingPlayer.isAlive()) {
                event.setCancelled(true);
            } else if (getEntitySet().contains(mob.getUniqueId())) {
                HotbarManager hotbarManager = damagingPlayer.getHotbarManager();
                HotbarObject hotbarObject = hotbarManager.getSelectedObject();

                if (hotbarObject instanceof MeleeWeapon) {
                    MeleeWeapon<?, ?> meleeWeapon = (MeleeWeapon<?, ?>) hotbarObject;
                    event.setCancelled(true);

                    if (meleeWeapon.isUsable()) {
                        meleeWeapon.attack(mob);
                    }
                }
            }
        }
    }

    private void onPlayerDeath(ProxyArgs<PlayerDeathEvent> args) {
        args.getEvent().setCancelled(true); //cancel death event

        if (state == ZombiesArenaState.STARTED) {
            ZombiesPlayer knocked = args.getManagedPlayer();

            if (knocked != null && knocked.getState() == ZombiesPlayerState.ALIVE) {
                knocked.knock();

                for (ZombiesPlayer player : getPlayerMap().values()) {
                    Player bukkitPlayer = player.getPlayer();

                    if (player.isAlive() && bukkitPlayer != null) {
                        RoomData knockedRoom = map.roomAt(bukkitPlayer.getLocation().toVector());
                        String message = knockedRoom == null ? "an unknown room" : knockedRoom.getRoomDisplayName();

                        //display death message only if necessary
                        for (ZombiesPlayer otherPlayer : getPlayerMap().values()) {
                            Player otherBukkitPlayer = otherPlayer.getPlayer();

                            if (otherPlayer != knocked && otherBukkitPlayer != null) {
                                otherBukkitPlayer.showTitle(Title.title(Component.text(bukkitPlayer.getName())
                                        .color(TextColor.color(255, 255, 0)), Component.text("was knocked down in " + message)
                                        .color(TextColor.color(61, 61, 61)), Title.Times.of(Duration.ofSeconds(1),
                                        Duration.ofSeconds(3), Duration.ofSeconds(1))));


                                otherPlayer.getPlayer().playSound(Sound.sound(
                                        Key.key("minecraft:entity.ender_dragon.growl"),
                                        Sound.Source.MASTER,
                                        1.0F,
                                        0.5F
                                ));
                            }
                        }

                        return; //return if there are any players still alive
                    }
                }

                doLoss(); //there are no players alive, so end the game

                // Bit hacky way to make sure corpses are registered to a team before their holograms are destroyed
                for (ZombiesPlayer player : getPlayerMap().values()) {
                    player.kill();
                    Corpse corpse = player.getCorpse();
                    if (corpse != null) {
                        corpse.terminate();
                    }
                }
            }
        }
    }

    private void onPlayerInteract(ProxyArgs<PlayerInteractEvent> args) {
        PlayerInteractEvent event = args.getEvent();
        ZombiesPlayer player = args.getManagedPlayer();

        if(player != null && event.getHand() == EquipmentSlot.HAND && player.isAlive()) {
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

        event.setCancelled(true);
    }

    @org.bukkit.event.EventHandler
    private void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();

        if (entity.getWorld().equals(world) && entity instanceof ItemFrame) {
            event.setCancelled(true);
        }
    }

    private void onPlayerInteractAtEntity(ProxyArgs<PlayerInteractAtEntityEvent> args) {
        PlayerInteractAtEntityEvent event = args.getEvent();
        ZombiesPlayer player = args.getManagedPlayer();

        if (player != null && event.getHand() == EquipmentSlot.HAND && player.isAlive()) {
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

    private void onPlayerSwapHandItems(ProxyArgs<PlayerSwapHandItemsEvent> args) {
        args.getEvent().setCancelled(true);
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

    private void onPlayerMove(ProxyArgs<PlayerMoveEvent> args) {
        ZombiesPlayer managedPlayer = args.getManagedPlayer();

        if(managedPlayer != null && managedPlayer.getState() == ZombiesPlayerState.KNOCKED) {
            //disgusting! but works (allows head rotation but not movement)
            if(!args.getEvent().getFrom().toVector().equals(args.getEvent().getTo().toVector())) {
                args.getEvent().setCancelled(true);
            }
        }
    }

    private void onPlayerAnimation(ProxyArgs<PlayerAnimationEvent> args) {
        PlayerAnimationEvent event = args.getEvent();
        ZombiesPlayer player = args.getManagedPlayer();

        // why does Bukkit only have one animation type?
        if (player != null && event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
            player.getHotbarManager().click(Action.LEFT_CLICK_BLOCK);
        }
    }

    private void onPlayerToggleSneak(ProxyArgs<PlayerToggleSneakEvent> args) {
        PlayerToggleSneakEvent event = args.getEvent();
        ZombiesPlayer managedPlayer = args.getManagedPlayer();

        if(managedPlayer != null) {
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
    }

    private void onPlayerItemHeld(ProxyArgs<PlayerItemHeldEvent> args) {
        PlayerItemHeldEvent event = args.getEvent();
        ZombiesPlayer managedPlayer = args.getManagedPlayer();

        if(managedPlayer != null) {
            managedPlayer.getHotbarManager().setSelectedSlot(event.getNewSlot());
        }
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

    private void onFoodLevelChange(ProxyArgs<FoodLevelChangeEvent> args) {
        FoodLevelChangeEvent event = args.getEvent();
        event.setCancelled(true);
    }

    private void onPlayerInventoryClick(ProxyArgs<InventoryClickEvent> args) {
        InventoryClickEvent event = args.getEvent();
        ZombiesPlayer managedPlayer = getPlayerMap().get(event.getWhoClicked().getUniqueId());
        if (managedPlayer != null) {
            Player player = managedPlayer.getPlayer();

            if (player != null && player.getInventory().equals(event.getClickedInventory())) {
                event.setCancelled(true);
            }
        }
    }

    public void startGame() {
        if(state == ZombiesArenaState.PREGAME || state == ZombiesArenaState.COUNTDOWN) {
            loadShops();

            state = ZombiesArenaState.STARTED;
            startTimeStamp = System.currentTimeMillis();

            for(ZombiesPlayer zombiesPlayer : getPlayerMap().values()) {
                Player bukkitPlayer = zombiesPlayer.getPlayer();

                if(bukkitPlayer != null) {
                    bukkitPlayer.sendMessage(ChatColor.YELLOW + "Started!");
                    zombiesPlayer.setAliveState();

                    Vector spawn = map.getSpawn();
                    zombiesPlayer.getPlayer().teleport(
                            new Location(world, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5)
                    );

                    ZombiesHotbarManager hotbarManager = zombiesPlayer.getHotbarManager();
                    for (Map.Entry<String, Set<Integer>> hotbarObjectGroupSlot : map.getHotbarObjectGroupSlots().entrySet()) {
                        hotbarManager.addEquipmentObjectGroup(equipmentManager
                                .createEquipmentObjectGroup(hotbarObjectGroupSlot.getKey(), zombiesPlayer.getPlayer(),
                                        hotbarObjectGroupSlot.getValue()));
                    }

                    for(String equipment : map.getDefaultEquipments()) {
                        EquipmentData<?> equipmentData = equipmentManager.getEquipmentData(map.getName(), equipment);
                        Integer slot
                                = hotbarManager.getHotbarObjectGroup(equipmentData.getEquipmentType()).getNextEmptySlot();

                        if (slot != null) {
                            hotbarManager.setHotbarObject(
                                    slot,
                                    equipmentManager.createEquipment(this, zombiesPlayer, slot, equipmentData)
                            );
                        }
                    }

                    zombiesPlayer.startTasks();
                }
            }

            doRound();
        }
    }

    private void doRound() {
        //respawn players
        getPlayerMap().values().stream().filter(player -> !player.isAlive()).forEach(ZombiesPlayer::respawn);

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

                runTaskLater(cumulativeDelay, () -> {
                    List<ActiveMob> spawnedMobs = spawner.spawnMobs(wave.getSpawnEntries(), wave.getMethod(),
                            wave.getSlaSquared(), wave.isRandomizeSpawnpoints(), false);

                    for(ActiveMob activeMob : spawnedMobs) {
                        MetadataHelper.setMetadataFor(activeMob.getEntity().getBukkitEntity(),
                                Zombies.SPAWNINFO_WAVE_METADATA_NAME, Zombies.getInstance(), wave);
                    }
                });
            }

            currentRoundProperty.setValue(this, currentRoundIndex + 1);
            getPlayerMap().forEach((l,r) -> {
                Player bukkitPlayer = r.getPlayer();
                if(bukkitPlayer != null) {
                    var messageTitle = currentRound.getCustomMessage() != null && !currentRound.getCustomMessage().isEmpty() ?
                            currentRound.getCustomMessage() : ChatColor.RED + "ROUND " + (currentRoundIndex + 1);
                    bukkitPlayer.sendTitle(messageTitle, "");
                    bukkitPlayer.playSound(Sound.sound(
                            Key.key("minecraft:entity.wither.spawn"),
                            Sound.Source.MASTER,
                            1.0F,
                            0.5F
                    ));
                }
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

    /**
     * Win code here
     */
    private void doVictory() {
        state = ZombiesArenaState.ENDED;
        endTimeStamp = System.currentTimeMillis();
        var round = map.getCurrentRoundProperty().getValue(this);
        getPlayerMap().forEach((l,r) -> {
            Player bukkitPlayer = r.getPlayer();

            if(bukkitPlayer != null) {
                r.getPlayer().sendTitle(ChatColor.GREEN + "You Win!", ChatColor.GRAY + "You made it to Round " + round + "!");
                r.getPlayer().sendMessage(ChatColor.YELLOW + "Zombies" + ChatColor.GRAY + " - " + ChatColor.RED + "You probably wanna change this after next beta");
            }
        });
        runTaskLater(200L, this::dispose);
    }

    /**
     * Loss code here
     */
    private void doLoss() {
        state = ZombiesArenaState.ENDED;
        endTimeStamp = System.currentTimeMillis();
        var round = map.getCurrentRoundProperty().getValue(this);
        getPlayerMap().forEach((l,r) -> {
            Player bukkitPlayer = r.getPlayer();
            if(bukkitPlayer != null) {
                bukkitPlayer.sendTitle(ChatColor.GREEN + "Game Over!", ChatColor.GRAY + "You made it to Round " + round + "!");
                bukkitPlayer.sendMessage(ChatColor.YELLOW + "Zombies" + ChatColor.GRAY + " - " + ChatColor.RED + "You probably wanna change this after next beta");
                bukkitPlayer.sendActionBar(Component.text());
            }
        });
        gameScoreboard.run();
        runTaskLater(200L, this::dispose);
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

        for (Shop<?> shop : shopMap.get(ShopType.TEAM_MACHINE)) {
            TeamMachine teamMachine = (TeamMachine) shop;
            getResourceManager().addDisposable(teamMachine);
        }

        Event<ShopEventArgs> chestEvent = shopEvents.get(ShopType.LUCKY_CHEST);
        if (chestEvent != null) {
            chestEvent.registerHandler(new EventHandler<>() {

                private final Random random = new Random();
                int rolls = 0;

                {
                    List<Shop<?>> chests = new ArrayList<>(shopMap.get(ShopType.LUCKY_CHEST));
                    LuckyChest luckyChest
                            = (LuckyChest) chests.get(random.nextInt(chests.size()));
                    luckyChest.setActive(true);

                    RoomData room = map.roomAt(luckyChest.getShopData().getChestLocation());
                    luckyChestRoom = room != null ? room.getName() : null;
                }

                @Override
                public void handleEvent(ShopEventArgs args) {
                    if (++rolls == map.getRollsPerChest()) {
                        LuckyChest luckyChest = (LuckyChest) args.getShop();
                        luckyChest.setActive(false);
                        List<Shop<?>> chests = new ArrayList<>(shopMap.get(luckyChest.getShopType()));
                        chests.remove(luckyChest);

                        ((LuckyChest) chests.get(random.nextInt(chests.size()))).setActive(true);
                        RoomData room = map.roomAt(luckyChest.getShopData().getChestLocation());
                        luckyChestRoom = room != null ? room.getName() : null;

                        rolls = 0;
                    }
                }
            });
        }
    }
}
