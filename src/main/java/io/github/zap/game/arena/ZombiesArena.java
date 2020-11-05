package io.github.zap.game.arena;

import io.github.zap.ZombiesPlugin;
import io.github.zap.event.player.PlayerJoinArenaEvent;
import io.github.zap.event.player.PlayerLeaveArenaEvent;
import io.github.zap.event.player.PlayerRightClickEvent;
import io.github.zap.game.Tickable;
import io.github.zap.game.data.MapData;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.util.List;

public class ZombiesArena extends Arena implements Tickable, Listener {
    private final ZombiesPlugin zombiesPlugin;
    private final PluginManager pluginManager;

    @Getter
    private final MapData map;

    @Getter
    private final long timeout;

    private int timeoutTaskId = -1;

    public ZombiesArena(MapData map, World world, long timeout) {
        super(world);
        this.map = map;
        this.timeout = timeout;

        zombiesPlugin = ZombiesPlugin.getInstance();
        pluginManager = zombiesPlugin.getServer().getPluginManager();

        pluginManager.registerEvents(this, zombiesPlugin);
        zombiesPlugin.getTicker().register(this);
    }

    @Override
    public String getName() {
        return world.getName();
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
            int newSize = players.size() + joiningPlayers.size();

            if(newSize <= map.getMaximumCapacity()) { //we can fit the players
                switch (state) {
                    case PREGAME:
                        if(newSize >= map.getMinimumCapacity()) {
                            startCountdown();
                        }
                        break;
                    case STARTED:
                        if(!map.isJoinableStarted()) {
                            return false;
                        }
                        break;
                }

                resetTimeout();
                players.addAll(joiningPlayers);
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
            players.removeAll(leavingPlayers);
            int currentSize = players.size();

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

                    if(currentSize < map.getMinimumCapacity()) {
                        cancelCountdown();
                    }
                    break;
                case STARTED:
                    if(currentSize == 0) {
                        if(map.isJoinableStarted()) {
                            close(); //close if nobody can rejoin
                        }
                        else {
                            startTimeout();
                        }
                    }
                    break;
            }

            pluginManager.callEvent(new PlayerLeaveArenaEvent(this, leavingPlayers, false));
        }
    }

    public void close() {
        PlayerRightClickEvent.getHandlerList().unregister(this);
        zombiesPlugin.getTicker().remove(this);
        zombiesPlugin.getArenaManager().removeArena(getName());
        zombiesPlugin.getWorldLoader().unloadWorld(world.getName());
    }

    @Override
    public void doTick() {
        for(Player player : players) {

        }
    }

    @EventHandler
    private void onPlayerRightClick(PlayerRightClickEvent event) {
        if(players.contains(event.getPlayer())) {

        }
    }

    private void startTimeout() {
        if(timeoutTaskId == -1) {
            timeoutTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(ZombiesPlugin.getInstance(), this::close, timeout);
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

    private void cancelCountdown() {

    }
}