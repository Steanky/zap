package io.github.zap.game.arena;

import com.google.common.collect.Lists;
import io.github.zap.ZombiesPlugin;
import io.github.zap.event.player.PlayerJoinArenaEvent;
import io.github.zap.event.player.PlayerLeaveArenaEvent;
import io.github.zap.event.player.PlayerRightClickEvent;
import io.github.zap.game.AccessorManager;
import io.github.zap.game.data.MapData;
import lombok.Getter;
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
public class ZombiesArena extends Arena implements Listener {
    private final ZombiesPlugin zombiesPlugin;
    private final PluginManager pluginManager;

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

    private int timeoutTaskId = -1;

    /**
     * Creates a new ZombiesArena with the specified map, world, and timeout.
     * @param map The map to use
     * @param world The world to use
     * @param emptyTimeout The time it will take the arena to close, if it is empty and in the pregame state
     */
    public ZombiesArena(MapData map, World world, long emptyTimeout) {
        super(world);
        this.map = map;
        this.emptyTimeout = emptyTimeout;

        zombiesPlugin = ZombiesPlugin.getInstance();
        pluginManager = zombiesPlugin.getServer().getPluginManager();

        zombiesPlugin.getTicker().register(this);
        pluginManager.registerEvents(this, zombiesPlugin);
    }

    public boolean handleJoin(JoinInformation joinAttempt) {
        List<Player> joiningPlayers = joinAttempt.getPlayers();

        if(joinAttempt.isSpectator()) {
            if(map.isSpectatorsAllowed()) {
                spectators.addAll(joiningPlayers);
                pluginManager.callEvent(new PlayerJoinArenaEvent(this, joiningPlayers, true));
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
                        if(!map.isJoinableStarted()) { //support players joining midgame
                            return false;
                        }
                        break;
                }

                resetTimeout(); //reset timeout task
                addPlayers(joiningPlayers);
                pluginManager.callEvent(new PlayerJoinArenaEvent(this, joiningPlayers, false));
                return true;
            }
        }

        return false;
    }

    public void handleLeave(LeaveInformation leaveAttempt) {
        List<Player> leavingPlayers = leaveAttempt.getPlayers();

        if(leaveAttempt.isSpectator()) {
            spectators.removeAll(leavingPlayers);
            pluginManager.callEvent(new PlayerLeaveArenaEvent(this, leavingPlayers, true));
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

            pluginManager.callEvent(new PlayerLeaveArenaEvent(this, leavingPlayers, false));
        }
    }

    public void close() {
        PlayerRightClickEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);

        zombiesPlugin.getTicker().remove(this);
        zombiesPlugin.getArenaManager().removeArena(getName());
        zombiesPlugin.getWorldLoader().unloadWorld(world.getName());

        AccessorManager.getInstance().removeMappingsFor(this);
    }

    @Override
    public void onTick() {
        for(ZombiesPlayer player : players) {
            player.onPlayerTick();
        }
    }

    @EventHandler
    private void onPlayerRightClick(PlayerRightClickEvent event) {
        ZombiesPlayer player = playerMap.getOrDefault(event.getPlayer().getUniqueId(), null);

        if(player != null) {
            player.playerRightClick(event);
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player bukkitPlayer = event.getPlayer();
        ZombiesPlayer zombiesPlayer = playerMap.get(bukkitPlayer.getUniqueId());

        if(zombiesPlayer != null) {
            handleLeave(new LeaveInformation(Lists.newArrayList(zombiesPlayer.getPlayer()), false));
        }
        else if(spectators.contains(bukkitPlayer)) {
            handleLeave(new LeaveInformation(Lists.newArrayList(bukkitPlayer), true));
        }
    }

    private void addPlayers(Iterable<Player> players) {
        for(Player player : players) {
            this.playerMap.put(player.getUniqueId(), new ZombiesPlayer(this, player, map.getStartingCoins()));
        }
    }

    private void removePlayers(Iterable<Player> players) {
        for(Player player : players) {
            this.playerMap.remove(player.getUniqueId());
        }
    }

    private void startTimeout() {
        if(timeoutTaskId == -1) {
            timeoutTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(ZombiesPlugin.getInstance(), this::close,
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