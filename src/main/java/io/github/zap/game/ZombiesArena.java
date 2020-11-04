package io.github.zap.game;

import io.github.zap.ZombiesPlugin;
import io.github.zap.game.data.MapData;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

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
        zombiesPlugin.getServer().getPluginManager().registerEvents(this, zombiesPlugin);
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
            }
        }
        else {
            int newSize = players.size() + joiningPlayers.size();

            if(newSize <= map.getMaximumCapacity()) { //we can fit the players
                switch (state) {
                    case PREGAME:
                    case COUNTDOWN:
                        players.addAll(joiningPlayers);
                        break;
                    case STARTED:
                        if(map.isInProgressJoin()) {
                            players.addAll(joiningPlayers);
                        }
                        break;
                }
            }
        }

        return false;
    }

    public void handleDisconnect(JoinInformation leaveAttempt) {
        Set<Player> leavingPlayers = leaveAttempt.getPlayers();
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

    @Override
    public void doTick() {

    }

    public void close() {
        ZombiesPlugin zombiesPlugin = ZombiesPlugin.getInstance();
        zombiesPlugin.getArenaManager().getArenas().remove(this);
        zombiesPlugin.getTicker().remove(this);
        zombiesPlugin.getWorldLoader().unloadWorld(getWorld().getName());
    }

    private void startCountdown() {

    }

    private void cancelCountdown() {

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

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        //noinspection StatementWithEmptyBody
        if(players.contains(event.getPlayer())) {
            //proxy to custom event handling system
        }
    }
}