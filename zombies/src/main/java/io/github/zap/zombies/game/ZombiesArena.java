package io.github.zap.zombies.game;

import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.game.arena.Arena;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.arenaapi.game.arena.LeaveInformation;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.MessageKey;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.*;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

/**
 * Encapsulates an active Zombies game and handles most related logic.
 */
public class ZombiesArena extends Arena<ZombiesArena> implements Listener {
    @Getter
    private final Map<UUID, ZombiesPlayer> zombiesPlayerMap = new HashMap<>();

    @Getter
    private final Collection<ZombiesPlayer> zombiesPlayers = zombiesPlayerMap.values();

    @Getter
    private final Set<UUID> spectators = new HashSet<>();

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
     * @param map The map to use
     * @param world The world to use
     * @param emptyTimeout The time it will take the arena to close, if it is empty and in the pregame state
     */
    public ZombiesArena(ZombiesArenaManager manager, World world, MapData map, long emptyTimeout) {
        super(manager, world);
        this.map = map;
        this.emptyTimeout = emptyTimeout;

        Bukkit.getPluginManager().registerEvents(this, Zombies.getInstance());
    }

    public boolean handleJoin(JoinInformation joinAttempt) {
        Set<UUID> joiningPlayers = joinAttempt.getPlayers();

        if(joinAttempt.isSpectator()) {
            if(map.isSpectatorAllowed()) {
                spectators.addAll(joiningPlayers);
                return true;
            }
        }
        else {
            int newSize = zombiesPlayerMap.size() + joiningPlayers.size();

            if(newSize <= map.getMaximumCapacity()) { //we can fit the players
                switch (state) {
                    case PREGAME:
                        if(newSize >= map.getMinimumCapacity()) {
                            startCountdown(); //we have enough to start
                        }
                        break;
                    case STARTED:
                        if(!map.isJoinableStarted()) { //support players joining midgame..?
                            return false;
                        }
                        break;
                }

                resetTimeout(); //reset timeout task
                addPlayers(joiningPlayers);
                return true;
            }
        }

        return false;
    }

    @Override
    public void handleLeave(LeaveInformation leaveAttempt) {
        Set<UUID> leavingPlayers = leaveAttempt.getPlayers();

        if(leaveAttempt.isSpectator()) {
            spectators.removeAll(leavingPlayers);
        }
        else {
            removePlayers(leavingPlayers);
            int currentSize = zombiesPlayerMap.size();

            switch(state) {
                case PREGAME:
                    if(currentSize == 0) {
                        startTimeout();
                    }
                    break;
                case COUNTDOWN:
                    if(currentSize == 0) {
                        startTimeout();
                    }

                    if(currentSize < map.getMinimumCapacity()) { //cancel countdown if too many people left
                        resetCountdown();
                    }
                    break;
                case STARTED:
                    if(currentSize == 0) {
                        if(!map.isJoinableStarted()) {
                            close(); //close immediately if nobody can rejoin
                        }
                        else {
                            startTimeout(); //otherwise, wait
                        }
                    }
                    break;
            }

        }
    }

    @Override
    public void terminate() { //non-graceful termination; might happen mid game, sends error messages
        Zombies.warning(String.format("Arena '%s', belonging to manager '%s', was terminated.", id.toString(),
                manager.getGameName()));

        for(ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            if(zombiesPlayer.isInGame()) {
                Zombies.sendLocalizedMessage(zombiesPlayer.getPlayer(), MessageKey.ARENA_TERMINATION);
            }
        }

        close();
    }

    /**
     * Whenever a death occurs, check player state (we may have a fail condition)
     */
    public void checkPlayerState() {
        for(ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            if(zombiesPlayer.isAlive()) {
                return;
            }
        }

        //game loss code here
    }

    public List<ActiveMob> spawnMobs(List<MythicMob> mobs, Spawner spawner) {
        List<ActiveMob> activeMobs = new ArrayList<>();

        for(RoomData room : map.getRooms()) {
            if(room.isSpawn() || room.getOpenProperty().get(this)) {
                do {
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

                                if(mobs.size() == 0) { //avoid redundant iteration with empty mobs list
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
                while(mobs.size() > 0);
            }
        }

        return activeMobs;
    }

    @EventHandler
    private void onMobDeath(EntityDeathEvent event) {
        if(mobs.remove(event.getEntity().getUniqueId())) {
            if(mobs.size() == 0) { //round ended, begin next one
                doRound();
            }
        }
    }

    /**
     * Used internally to "gracefully" shut down the arena — without sending error messages to the player.
     */
    private void close() {
        //unregister events
        EntityDeathEvent.getHandlerList().unregister(this);

        //unregister tasks
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.cancelTask(timeoutTaskId);

        for(int taskId : waveSpawnerTasks) { //usually won't get run unless we just terminated
            scheduler.cancelTask(taskId);
        }

        //close players
        for(ZombiesPlayer player : zombiesPlayers) {
            player.close();
        }

        //cleanup mappings and remove arena from manager
        Property.removeMappingsFor(this);
        manager.removeArena(this);
    }

    private void addPlayers(Collection<UUID> players) {
        for(UUID player : players) {
            Player bukkitPlayer = Bukkit.getPlayer(player);

            if(bukkitPlayer != null) {
                if(!zombiesPlayerMap.containsKey(player)) {
                    zombiesPlayerMap.put(player, new ZombiesPlayer(this, bukkitPlayer, map.getStartingCoins()));
                }
                else {
                    ZombiesPlayer zombiesPlayer = zombiesPlayerMap.get(player);
                    zombiesPlayer.rejoin();
                }

                bukkitPlayer.teleport(WorldUtils.locationFrom(world, map.getSpawn()));
            }
            else {
                Zombies.getInstance().getLogger().warning(String.format("When attempting to add players to " +
                        "ZombiesArena, UUID %s was not found.", player.toString()));
            }
        }
    }

    private void removePlayers(Collection<UUID> players) {
        for(UUID player : players) {
            ZombiesPlayer zombiesPlayer = zombiesPlayerMap.get(player);
            zombiesPlayer.quit();

            //teleport player to destination lobby
        }
    }

    private void startTimeout() {
        if(timeoutTaskId == -1) {
            timeoutTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(Zombies.getInstance(), this::close,
                    emptyTimeout);
        }
    }

    private void resetTimeout() {
        if(timeoutTaskId != -1) {
            Bukkit.getScheduler().cancelTask(timeoutTaskId);
            timeoutTaskId = -1;
        }
    }

    private void startCountdown() {
        //do countdown timer; at the end, call doRound() to kick off the game
    }

    private void resetCountdown() {
        //reset countdown timer
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

                waveSpawnerTasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(Zombies.getInstance(), () -> {
                    spawnMobs(wave.getMobs(), spawner);
                    waveSpawnerTasks.remove(0);
                }, cumulativeDelay));
            }

            currentRoundProperty.set(this, currentRoundIndex + 1);
        }
        else {
            //game just finished, do win condition

            close();
        }
    }
}