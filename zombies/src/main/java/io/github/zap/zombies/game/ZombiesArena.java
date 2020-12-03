package io.github.zap.zombies.game;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.PlayerMessageHandler;
import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.game.arena.Arena;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.arenaapi.game.arena.LeaveInformation;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.MessageKeys;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.event.player.PlayerJoinArenaEvent;
import io.github.zap.zombies.event.player.PlayerLeaveArenaEvent;
import io.github.zap.zombies.game.data.MapData;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
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
    private final Set<Player> spectators = new HashSet<>();

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
        pluginManager.registerEvents(this, zombies);
    }

    public boolean handleJoin(JoinInformation joinAttempt) {
        Player[] joiningPlayers = joinAttempt.getPlayers();

        if(joinAttempt.isSpectator()) {
            if(map.isSpectatorAllowed()) {
                Collections.addAll(spectators, joiningPlayers);
                pluginManager.callEvent(new PlayerJoinArenaEvent(joinAttempt));
                return true;
            }
        }
        else {
            int newSize = playerMap.size() + joiningPlayers.length;

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
        Player[] leavingPlayers = leaveAttempt.getPlayers();

        if(leaveAttempt.isSpectator()) {
            spectators.removeAll(Arrays.asList(leavingPlayers));
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

        pluginManager.callEvent(new PlayerLeaveArenaEvent(leaveAttempt));
    }

    @Override
    public void terminate() {
        for(ZombiesPlayer zombiesPlayer : players) {
            PlayerMessageHandler.sendLocalizedMessage(zombiesPlayer.getPlayer(),
                    MessageKeys.ARENA_TERMINATION.getKey());
        }

        close();
    }

    /**
     * Used internally to "gracefully" shut down the arena — without sending error messages to the player.
     */
    private void close() {
        PlayerQuitEvent.getHandlerList().unregister(this);

        for(ZombiesPlayer player : players) {
            player.close();
        }

        Property.removeMappingsFor(this);

        manager.removeArena(this);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        //noinspection StatementWithEmptyBody
        if(playerMap.containsKey(event.getPlayer().getUniqueId())) {
            //handle players quitting
        }
    }

    @SneakyThrows
    private void addPlayers(Player[] players) {
        for(Player player : players) {
            playerMap.put(player.getUniqueId(), new ZombiesPlayer(this, player, map.getStartingCoins()));
            player.teleport(WorldUtils.locationFrom(world, map.getSpawn()));
        }
    }

    private void removePlayers(Player[] players) {
        for(Player player : players) {
            playerMap.remove(player.getUniqueId()).close();

            //teleport to destination lobby
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