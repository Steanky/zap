package io.github.zap.game.arena;

import io.github.zap.ZombiesPlugin;
import io.github.zap.event.player.PlayerJoinEvent;
import io.github.zap.event.player.PlayerLeaveEvent;
import io.github.zap.event.player.PlayerRightClickEvent;
import io.github.zap.game.Tickable;
import io.github.zap.game.data.MapData;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.util.List;
import java.util.Set;

public class ZombiesArena extends Arena implements Tickable, Listener {
    private final ZombiesPlugin zombiesPlugin;
    private final PluginManager pluginManager;

    @Getter
    private final MapData map;

    public ZombiesArena(MapData map, World world) {
        super(world);
        this.map = map;

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
            if(map.isSpectatorAllowed()) {
                spectators.addAll(joiningPlayers);
                pluginManager.callEvent(new PlayerJoinEvent(this, joiningPlayers, true));
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
                        if(!map.isInProgressJoin()) {
                            return false;
                        }
                        break;
                }

                players.addAll(joiningPlayers);
                pluginManager.callEvent(new PlayerJoinEvent(this, joiningPlayers, false));
                return true;
            }
        }

        return false;
    }

    public void handleLeave(LeaveInformation leaveAttempt) {
        List<Player> leavingPlayers = leaveAttempt.getPlayers();

        if(leaveAttempt.isSpectator()) {
            spectators.removeAll(leavingPlayers);
            pluginManager.callEvent(new PlayerLeaveEvent(this, leavingPlayers, true));
        }
        else {
            players.removeAll(leavingPlayers);
            int currentSize = players.size();
            switch(state) {
                case PREGAME:
                    if(currentSize == 0) {
                        //TODO: if there are no players, we should shut down this arena eventually but not immediately
                    }
                    break;
                case COUNTDOWN:
                    if(currentSize < map.getMinimumCapacity()) {
                        cancelCountdown();
                    }
                    break;
                case STARTED:
                    if(currentSize == 0) {
                        close(); //immediately close this arena if the game was in progress
                    }
                    break;
            }

            pluginManager.callEvent(new PlayerLeaveEvent(this, leavingPlayers, false));
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
        //gameloop
    }

    @EventHandler
    private void onPlayerRightClick(PlayerRightClickEvent event) {

    }

    private void startCountdown() {

    }

    private void cancelCountdown() {

    }
}