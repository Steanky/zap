package io.github.zap.zombies.game;

import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.event.EventHandler;
import io.github.zap.arenaapi.event.ProxyEvent;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.equipment.EquipmentManager;
import io.github.zap.zombies.game.data.map.*;
import io.github.zap.zombies.game.data.map.shop.ShopData;
import io.github.zap.zombies.game.data.map.shop.ShopManager;
import io.github.zap.zombies.game.shop.LuckyChest;
import io.github.zap.zombies.game.shop.Shop;
import io.github.zap.zombies.game.shop.ShopEventArgs;
import io.github.zap.zombies.game.shop.ShopType;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitWorld;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Encapsulates an active Zombies game and handles most related logic.
 */
public class ZombiesArena extends ManagingArena<ZombiesArena, ZombiesPlayer> implements Listener {
    private interface Spawner {
        /**
         * Spawns a wave.
         * @param waveData The wave data to spawn
         */
        void spawnWave(WaveData waveData);

        /**
         * Spawns mobs, outside of a wave.
         * @param mobs The
         */
        void spawnMobs(List<SpawnEntryData> mobs, SpawnMethod method, int slaSquared, boolean randomize);
    }

    /**
     * Basic spawner implementation.
     */
    @RequiredArgsConstructor
    private class BasicSpawner implements Spawner {
        @Override
        public void spawnWave(WaveData wave) {
            spawnMobs(wave.getSpawnEntries(), wave.getMethod(), wave.getSlaSquared(), wave.isRandomizeSpawnpoints());
        }

        @Override
        public void spawnMobs(List<SpawnEntryData> mobs, SpawnMethod method, int slaSquared, boolean randomize) {
            List<SpawnpointData> spawnpoints = filterSpawnpoints(mobs, method, slaSquared);

            if(spawnpoints.size() == 0) {
                Zombies.warning("There are no available spawnpoints for this mob set. This likely indicates an error " +
                        "in map configuration.");
                return;
            }

            if(randomize) {
                Collections.shuffle(spawnpoints); //shuffle small candidate set of spawnpoints
            }

            for(SpawnEntryData spawnEntryData : mobs) {
                int amt = spawnEntryData.getMobCount();

                for(SpawnpointData spawnpointData : spawnpoints) {
                    if(spawnpointData.canSpawn(spawnEntryData.getMobName())) {
                        spawnMob(spawnEntryData.getMobName(), spawnpointData.getSpawn());
                        amt--;
                    }

                    if(amt == 0) {
                        break;
                    }
                }
            }
        }

        /**
         * This rather interesting function filters only the spawnpoints that matter for this particular operation.
         * Spawnpoints that are in closed rooms, spawnpoints that can't spawn any of the mobs in this
         * wave, and spawnpoints that are out of range are all filtered out, leaving only the important ones.
         * we do all this filtering to minimize the set of spawnpoints that we have to shuffle
         * @param mobs The mobs to spawn
         * @param method The SpawnMethod to use
         * @param slaSquared
         * @return
         */
        private List<SpawnpointData> filterSpawnpoints(List<SpawnEntryData> mobs, SpawnMethod method, int slaSquared) {
            return map.getRooms().stream().filter(roomData -> roomData.isSpawn() || method == SpawnMethod.FORCE ||
                    roomData.getOpenProperty().getValue(ZombiesArena.this))
                    .flatMap(roomData -> roomData.getSpawnpoints().stream()).filter(spawnpointData -> mobs.stream()
                            .anyMatch(spawnEntryData -> spawnpointData.canSpawn(spawnEntryData.getMobName())))
                    .filter(spawnpointData -> getPlayerMap().values().stream().anyMatch(player -> method !=
                            SpawnMethod.RANGED || player.getPlayer().getLocation().toVector().distanceSquared(
                                    spawnpointData.getSpawn()) <= slaSquared)).collect(Collectors.toList());
        }
    }

    @Getter
    private final MapData map;

    @Getter
    private final EquipmentManager equipmentManager;

    @Getter
    private final ShopManager shopManager;

    @Getter
    protected ZombiesArenaState state = ZombiesArenaState.PREGAME;

    @Getter
    private final long emptyTimeout;

    @Getter
    private final Spawner spawner;

    @Getter
    private final Set<UUID> mobs = new HashSet<>();

    @Getter
    private final List<Shop<?>> shops = new ArrayList<>();

    @Getter
    private final Map<String, List<Shop<?>>> shopMap = new HashMap<>();

    @Getter
    private final Map<String, Event<ShopEventArgs>> shopEvents = new HashMap<>();

    private final List<Integer> waveSpawnerTasks = new ArrayList<>();
    private int timeoutTaskId = -1;

    /**
     * Creates a new ZombiesArena with the specified map, world, and timeout.
     *
     * @param map          The map to use
     * @param world        The world to use
     * @param emptyTimeout The time it will take the arena to close, if it is empty and in the pregame state
     */
    public ZombiesArena(ZombiesArenaManager manager, World world, MapData map, long emptyTimeout) {
        super(Zombies.getInstance(), manager, world, (arena, player) -> new ZombiesPlayer(arena, player,
                manager.getEquipmentManager()));

        this.map = map;
        this.equipmentManager = manager.getEquipmentManager();
        this.shopManager = manager.getShopManager();
        this.emptyTimeout = emptyTimeout;
        this.spawner = new BasicSpawner();

        Event<EntityDeathEvent> entityDeathEvent = new ProxyEvent<>(Zombies.getInstance(), this,
                EntityDeathEvent.class);
        entityDeathEvent.registerHandler(this::onMobDeath);

        getPlayerJoinEvent().registerHandler(this::onPlayerJoin);
        getPlayerLeaveEvent().registerHandler(this::onPlayerLeave);
        getPlayerDeathEvent().registerHandler(this::onPlayerDeath);
        getPlayerInteractEvent().registerHandler(this::onPlayerInteract);
        getPlayerInteractAtEntityEvent().registerHandler(this::onPlayerInteractAtEntity);
        getPlayerToggleSneakEvent().registerHandler(this::onPlayerSneak);
        getPlayerItemHeldEvent().registerHandler(this::onPlayerItemHeld);
        getPlayerItemConsumeEvent().registerHandler(this::onPlayerItemConsume);
        getPlayerAttemptPickupItemEvent().registerHandler(this::onPlayerAttemptPickupItem);
        getPlayerArmorStandManipulateEvent().registerHandler(this::onPlayerArmorStandManipulate);
    }

    @Override
    protected ZombiesArena getArena() {
        return this;
    }

    @Override
    public void dispose() {
        super.dispose(); //dispose of superclass-specific resources

        //unregister tasks
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.cancelTask(timeoutTaskId);

        for(int taskId : waveSpawnerTasks) {
            scheduler.cancelTask(taskId);
        }

        //cleanup mappings and remove arena from manager
        Property.removeMappingsFor(this);
    }

    @Override
    protected boolean allowPlayers() {
        return state != ZombiesArenaState.ENDED && (state != ZombiesArenaState.STARTED || map.isAllowRejoin());
    }

    @Override
    protected boolean allowPlayerJoin(List<Player> players) {
        return (state == ZombiesArenaState.PREGAME || state == ZombiesArenaState.COUNTDOWN) &&
                getOnlineCount() + players.size() <= map.getMaximumCapacity();
    }

    protected boolean allowPlayerRejoin(List<ZombiesPlayer> players) {
        return state == ZombiesArenaState.STARTED && map.isAllowRejoin();
    }

    private void onPlayerJoin(PlayerListArgs args) {
        if(state == ZombiesArenaState.PREGAME && getOnlineCount() >= map.getMinimumCapacity()) {
            state = ZombiesArenaState.COUNTDOWN;
            startCountdown();
        }

        for(Player player : args.getPlayers()) {
            player.teleport(WorldUtils.locationFrom(world, map.getSpawn()));
            player.setGameMode(GameMode.ADVENTURE);
        }

        resetTimeout(); //if arena was in timeout state, reset that
    }

    private void onPlayerLeave(ManagedPlayerListArgs args) {
        switch (state) {
            case PREGAME:
                if (getOnlineCount() == 0) {
                    startTimeout();
                }

                removePlayers(args.getPlayers());
                break;
            case COUNTDOWN:
                if (getOnlineCount() == 0) {
                    startTimeout();
                }
                if (getOnlineCount() < map.getMinimumCapacity()) {
                    state = ZombiesArenaState.PREGAME;
                    stopCountdown();
                }

                removePlayers(args.getPlayers());
                break;
            case STARTED:
                if (getOnlineCount() == 0) {
                    state = ZombiesArenaState.ENDED;
                    dispose(); //shut everything down immediately if everyone leaves mid-game
                }
                break;
        }

        if(!map.isAllowRejoin()) {
            for(ZombiesPlayer player : args.getPlayers()) {
                super.removePlayer(player);
            }
        }
    }

    private void onMobDeath(EntityDeathEvent args) {
        mobs.remove(args.getEntity().getUniqueId());
        if (mobs.size() == 0 && state == ZombiesArenaState.STARTED) { //round ended, begin next one
            doRound();
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

        doLoss();
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

    private void onPlayerSneak(ProxyArgs<PlayerToggleSneakEvent> args) {
        PlayerToggleSneakEvent event = args.getEvent();
        ZombiesPlayer managedPlayer = args.getManagedPlayer();

        if(event.isSneaking()) {
            managedPlayer.activateRepair();
        }
        else {
            managedPlayer.disableRepair();
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

    private void onPlayerAttemptPickupItem(ProxyArgs<PlayerAttemptPickupItemEvent> args) {
        args.getEvent().setCancelled(true);
    }

    private void onPlayerArmorStandManipulate(ProxyArgs<PlayerArmorStandManipulateEvent> args) {
        args.getEvent().setCancelled(true);
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

            long cumulativeDelay = 0;
            for (WaveData wave : currentRound.getWaves()) {
                cumulativeDelay += wave.getWaveLength();

                waveSpawnerTasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(Zombies.getInstance(), () -> {
                    spawner.spawnWave(wave);
                    waveSpawnerTasks.remove(0);
                }, cumulativeDelay));
            }

            currentRoundProperty.setValue(this, currentRoundIndex + 1);
        }
        else {
            //game just finished, do win condition
            state = ZombiesArenaState.ENDED;
            doVictory();
        }
    }

    /**
     * Spawns the mob at the specified vector.
     * @param mobName The name of the MythicMob to spawn
     * @param at The location to spawn the mob at, in this arena's world
     * @return Whether or not the mob was successfully spawned
     */
    public boolean spawnMob(String mobName, Vector at) {
        MythicMob mob = MythicMobs.inst().getMobManager().getMythicMob(mobName);

        if(mob != null) {
            ActiveMob activeMob = mob.spawn(new AbstractLocation(new BukkitWorld(world), at.getX(), at.getY(),
                    at.getZ()), map.getMobSpawnLevel());

            if(activeMob != null) {
                mobs.add(activeMob.getUniqueId());
                return true;
            }
            else {
                Zombies.warning(String.format("An error occurred while trying to spawn mob of type '%s'.", mobName));
            }
        }
        else {
            Zombies.warning(String.format("Mob type '%s' is not known.", mobName));
        }

        return false;
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

    public void startCountdown() {
        //do countdown timer; at the end, call doRound() to kick off the game
        // TODO: do this at the end
        for (ShopData shopData : map.getShops()) {
            Shop<?> shop = shopManager.createShop(this, shopData);
            shops.add(shop);
            shopMap.computeIfAbsent(shop.getShopType(), (String type) -> new ArrayList<>()).add(shop);
            shopEvents.computeIfAbsent(shopData.getType(), (String type) -> new Event<>());
        }
        Event<ShopEventArgs> chestEvent = shopEvents.get(ShopType.LUCKY_CHEST.name());
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

    public void stopCountdown() {
        //reset countdown timer
    }

    /**
     * Win code here
     */
    private void doVictory() {

    }

    /**
     * Loss code here
     */
    private void doLoss() {

    }
}
