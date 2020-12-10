package io.github.zap.zombies.game;

import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.game.arena.Arena;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.arenaapi.game.arena.LeaveInformation;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.MessageKey;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.MapData;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

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

    private int timeoutTaskId = -1;

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
     * Used internally to "gracefully" shut down the arena — without sending error messages to the player.
     */
    private void close() {
        for(ZombiesPlayer player : zombiesPlayers) {
            player.close();
        }

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

    }

    private void resetCountdown() {

    }
}