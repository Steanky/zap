package io.github.zap.zombies.game;

import io.github.zap.arenaapi.PlayerMessageHandler;
import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.game.arena.Arena;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.arenaapi.game.arena.LeaveInformation;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.MessageKeys;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.event.player.PlayerJoinArenaEvent;
import io.github.zap.zombies.event.player.PlayerQuitArenaEvent;
import io.github.zap.zombies.game.data.MapData;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.util.*;

/**
 * Encapsulates an active Zombies game and handles most related logic.
 */
public class ZombiesArena extends Arena<ZombiesArena> implements Listener {
    @Getter
    private final Map<UUID, ZombiesPlayer> playerMap = new HashMap<>();

    @Getter
    private final Collection<ZombiesPlayer> players = playerMap.values();

    @Getter
    private final Set<UUID> spectators = new HashSet<>();

    @Getter
    private final MapData map;

    @Getter
    protected ZombiesArenaState state = ZombiesArenaState.PREGAME;

    @Getter
    private final long emptyTimeout;

    private final PluginManager pluginManager;
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

        Zombies zombies = Zombies.getInstance();
        pluginManager = zombies.getServer().getPluginManager();
    }

    public boolean handleJoin(JoinInformation joinAttempt) {
        Set<UUID> joiningPlayers = joinAttempt.getPlayers();

        if(joinAttempt.isSpectator()) {
            if(map.isSpectatorAllowed()) {
                spectators.addAll(joiningPlayers);
                pluginManager.callEvent(new PlayerJoinArenaEvent(joinAttempt));
                return true;
            }
        }
        else {
            int newSize = playerMap.size() + joiningPlayers.size();

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
                pluginManager.callEvent(new PlayerJoinArenaEvent(joinAttempt));
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
            int currentSize = playerMap.size();

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
                        if(map.isJoinableStarted()) {
                            close(); //close immediately if nobody can rejoin
                        }
                        else {
                            startTimeout(); //otherwise, wait
                        }
                    }
                    break;
            }

        }

        pluginManager.callEvent(new PlayerQuitArenaEvent(leaveAttempt));
    }

    @Override
    public void terminate() { //non-graceful termination; might happen mid game, sends error messages
        for(ZombiesPlayer zombiesPlayer : players) {
            if(zombiesPlayer.isInGame()) {
                PlayerMessageHandler.sendLocalizedMessage(zombiesPlayer.getPlayer(),
                        MessageKeys.ARENA_TERMINATION.getKey());
            }
        }

        close();
    }

    /**
     * Used internally to "gracefully" shut down the arena — without sending error messages to the player.
     */
    private void close() {
        for(ZombiesPlayer player : players) {
            player.close();
        }

        Property.removeMappingsFor(this);
        manager.removeArena(this);
    }

    private void addPlayers(Collection<UUID> players) {
        for(UUID player : players) {
            Player bukkitPlayer = Bukkit.getPlayer(player);

            if(bukkitPlayer != null) {
                if(!playerMap.containsKey(player)) {
                    playerMap.put(player, new ZombiesPlayer(this, bukkitPlayer, map.getStartingCoins()));
                }
                else {
                    playerMap.get(player).setInGame(true); //player rejoined
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
            ZombiesPlayer zombiesPlayer = playerMap.get(player);
            zombiesPlayer.setInGame(false);

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