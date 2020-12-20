package io.github.zap.zombies.game;

import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.event.ProxyEvent;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.*;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

/**
 * Encapsulates an active Zombies game and handles most related logic.
 */
public class ZombiesArena extends ManagingArena<ZombiesArena, ZombiesPlayer> implements Listener {
    @Getter
    private final MapData map;

    @Getter
    protected ZombiesArenaState state = ZombiesArenaState.PREGAME;

    @Getter
    private final long emptyTimeout;

    @Getter
    private final Spawner spawner = new RangelessSpawner();

    @Getter
    private final Set<UUID> mobs = new HashSet<>();

    private int timeoutTaskId = -1;
    private final List<Integer> waveSpawnerTasks = new ArrayList<>();

    /**
     * Creates a new ZombiesArena with the specified map, world, and timeout.
     *
     * @param map          The map to use
     * @param world        The world to use
     * @param emptyTimeout The time it will take the arena to close, if it is empty and in the pregame state
     */
    public ZombiesArena(ZombiesArenaManager manager, World world, MapData map, long emptyTimeout) {
        super(Zombies.getInstance(), manager, world, (arena, player) -> new ZombiesPlayer(arena, player,
                arena.getMap().getStartingCoins()));

        this.map = map;
        this.emptyTimeout = emptyTimeout;

        Event<EntityDeathEvent> entityDeathEvent = new ProxyEvent<>(Zombies.getInstance(), this,
                (event) -> state == ZombiesArenaState.STARTED && mobs.contains(event.getEntity().getUniqueId()),
                EntityDeathEvent.class);
        entityDeathEvent.registerHandler(this::onMobDeath);

        getPlayerJoinEvent().registerHandler(this::onPlayerJoin);
        getPlayerLeaveEvent().registerHandler(this::onPlayerLeave);
    }
    @Override
    protected ZombiesArena getArena() {
        return this;
    }

    @Override
    public void close() {
        super.close(); //any events we register will get un-registered by the superclass. it's magic :)

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
        return state != ZombiesArenaState.ENDED;
    }
    @Override
    protected boolean allowPlayerJoin(List<Player> players) {
        return (state == ZombiesArenaState.PREGAME || state == ZombiesArenaState.COUNTDOWN) &&
                getOnlineCount() + players.size() <= map.getMaximumCapacity();
    protected boolean allowPlayerRejoin(List<ZombiesPlayer> players) {
        return state == ZombiesArenaState.STARTED && map.isAllowRejoin();
    }

    private void onPlayerJoin(Event<PlayerListArgs> caller, PlayerListArgs args) {
        if(state == ZombiesArenaState.PREGAME && getOnlineCount() >= map.getMinimumCapacity()) {
            state = ZombiesArenaState.COUNTDOWN;
            startCountdown();
        }
        for(Player player : args.getPlayers()) {
            player.teleport(WorldUtils.locationFrom(world, map.getSpawn()));
    }
    private void onPlayerLeave(Event<ManagedPlayerListArgs> caller, ManagedPlayerListArgs args) {
        switch (state) {
            case PREGAME:
                if(getOnlineCount() == 0) {
                    startTimeout();
                }

                removePlayers(args.getPlayers());
                break;
            case COUNTDOWN:
                if(getOnlineCount() == 0) {
                    startTimeout();
                }
                if(getOnlineCount() < map.getMinimumCapacity()) {
                    state = ZombiesArenaState.PREGAME;
                    stopCountdown();
                }

                removePlayers(args.getPlayers());
                break;
            case STARTED:
                if(getOnlineCount() == 0) {
                    if(map.isAllowRejoin()) {
                        startTimeout(); //if people can rejoin, wait a bit before closing
                    }
                    else {
                        close(); //otherwise, just close
                    }
                }
                break;
        }
        for(ZombiesPlayer player : args.getPlayers()) {
            player.getPlayer().teleport(manager.getHubLocation());
    private void onMobDeath(Event<EntityDeathEvent> caller, EntityDeathEvent args) {
        mobs.remove(args.getEntity().getUniqueId());
        if(mobs.size() == 0 && state == ZombiesArenaState.STARTED) { //round ended, begin next one
            doRound();

    public List<ActiveMob> spawnMobs(List<MythicMob> mobs, Spawner spawner) {
        List<ActiveMob> activeMobs = new ArrayList<>();

        for(RoomData room : map.getRooms()) {
            if(room.isSpawn() || room.getOpenProperty().get(this)) {
                while(true) {
                    boolean spawned = false;

                    for(SpawnpointData spawnpoint : room.getSpawnpoints()) {
                        for(int i = mobs.size() - 1; i >= 0; i--) {
                            MythicMob mythicMob = mobs.get(i);

                            if(spawner.canSpawn(this, spawnpoint, mythicMob)) {
                                ActiveMob activeMob = spawner.spawnAt(this, spawnpoint, mythicMob);
                                mobs.remove(i);

                                if(activeMob != null) {
                                    this.mobs.add(activeMob.getUniqueId());
                                    activeMobs.add(activeMob);
                                    spawned = true;
                                    break;
                                }

                                if(mobs.size() == 0) { //avoid redundant iteration when all mobs have been spawned
                                    return activeMobs;
                                }
                            }
                        }
                    }

                    if(!spawned) {
                        Zombies.warning("Some enemies could not be spawned.");
                        return activeMobs;
                    }
                }
            }
        }

        return activeMobs;
    }

    private void doRound() {
        Property<Integer> currentRoundProperty = map.getCurrentRoundProperty();
        int currentRoundIndex = currentRoundProperty.get(this);

        List<RoundData> rounds = map.getRounds();
        if(currentRoundIndex < rounds.size()) {
            RoundData currentRound = rounds.get(currentRoundIndex);

            long cumulativeDelay = 0;
            for (WaveData wave : currentRound.getWaves()) {
                cumulativeDelay += wave.getWaveLength();
                List<MythicMob> mobs = new ArrayList<>();

                for(String mobName : wave.getMobs()) {
                    mobs.add(MythicMobs.inst().getMobManager().getMythicMob(mobName));
                }

                waveSpawnerTasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(Zombies.getInstance(), () -> {
                    spawnMobs(mobs, spawner);
                    waveSpawnerTasks.remove(0);
                }, cumulativeDelay));
            }

            currentRoundProperty.set(this, currentRoundIndex + 1);
        }
        else {
            //game just finished, do win condition
            state = ZombiesArenaState.ENDED;
            close();
        }
    }
}