package io.github.zap.game;

import io.github.zap.ZombiesPlugin;
import io.github.zap.event.PlayerJoinEvent;
import io.github.zap.event.PlayerRightClickEvent;
import io.github.zap.game.data.MapData;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;

import java.util.HashSet;
import java.util.Set;

public class ZombiesArena implements Tickable, Listener {
    @Getter
    private final MapData map;

    @Getter
    private final World world;

    @Getter
    private final Set<Player> players = new HashSet<>();

    @Getter
    private final Set<Player> spectators = new HashSet<>();

    @Getter
    private ArenaState state = ArenaState.PREGAME;

    public ZombiesArena(MapData map, World world) {
        this.map = map;
        this.world = world;

        ZombiesPlugin zombiesPlugin = ZombiesPlugin.getInstance();
        PluginManager pluginManager = zombiesPlugin.getServer().getPluginManager();
        pluginManager.registerEvents(this, zombiesPlugin);
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof ZombiesArena) {
            return getName().equals(((ZombiesArena)other).getName());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * Tries to add a player or set of players represented by JoinInformation to this arena.
     * @param joinAttempt The join information
     * @return Whether or not the attempt was successful (all the players were added)
     */
    public boolean handleJoin(JoinInformation joinAttempt) {
        Set<Player> joiningPlayers = joinAttempt.getPlayers();

        if(joinAttempt.isAsSpectator()) {
            if(state != ArenaState.ENDED && map.isSpectatorAllowed()) {
                spectators.addAll(joiningPlayers);
                ZombiesPlugin.getInstance().getServer().getPluginManager().callEvent(new PlayerJoinEvent(joiningPlayers, true));
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
                        players.addAll(joiningPlayers);
                        ZombiesPlugin.getInstance().getServer().getPluginManager().callEvent(new PlayerJoinEvent(joiningPlayers, false));
                        return true;
                    case COUNTDOWN:
                        players.addAll(joiningPlayers);
                        ZombiesPlugin.getInstance().getServer().getPluginManager().callEvent(new PlayerJoinEvent(joiningPlayers, false));
                        return true;
                    case STARTED:
                        if(map.isInProgressJoin()) {
                            players.addAll(joiningPlayers);
                            ZombiesPlugin.getInstance().getServer().getPluginManager().callEvent(new PlayerJoinEvent(joiningPlayers, false));
                            return true;
                        }
                        return false;
                }
            }
        }

        return false;
    }

    public void handleDisconnect(JoinInformation leaveAttempt) {
        Set<Player> leavingPlayers = leaveAttempt.getPlayers();

        if(leaveAttempt.isAsSpectator()) {
            spectators.removeAll(leavingPlayers);
        }
    }

    /**
     * Gets the name of this arena.
     * @return The name of this arena, which is the same as the world name used to create it
     */
    @Override
    public String getName() {
        return world.getName();
    }

    /**
     * Gets the current player count.
     * @return The current player count
     */
    public int playerCount() {
        return players.size();
    }

    private void startCountdown() {

    }

    private void cancelCountdown() {

    }

    public void close() {
        ZombiesPlugin zombiesPlugin = ZombiesPlugin.getInstance();
        zombiesPlugin.getArenaManager().getArenas().remove(this);
        zombiesPlugin.getTicker().remove(this);
        zombiesPlugin.getWorldLoader().unloadWorld(getWorld().getName());
    }

    @Override
    public void doTick() {
        //gameloop
    }

    @EventHandler
    private void onPlayerRightClick(PlayerRightClickEvent event) {

    }
}