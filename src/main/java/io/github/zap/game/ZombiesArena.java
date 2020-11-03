package io.github.zap.game;

import io.github.zap.ZombiesPlugin;
import io.github.zap.game.data.MapData;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

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
    private ArenaState state = ArenaState.PREGAME;

    public ZombiesArena(MapData map, World world) {
        this.map = map;
        this.world = world;

        ZombiesPlugin zombiesPlugin = ZombiesPlugin.getInstance();
        zombiesPlugin.getServer().getPluginManager().registerEvents(this, zombiesPlugin);
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

    /**
     * Tries to add a player or set of players represented by JoinInformation to this arena.
     * @param attempt The join information
     * @return Whether or not the attempt was successful (all the players were added)
     */
    public boolean handleJoin(JoinInformation attempt) {
        Set<Player> newPlayers = attempt.getPlayers();
        int partySize = newPlayers.size();
        int currentSize = players.size();

        if(partySize + currentSize < map.getMaximumCapacity()) {
            for(Player player : newPlayers) {
                addPlayer(player);
            }

            return true;
        }
        else {
            return false;
        }
    }

    private void addPlayer(Player player) {
        players.add(player);

        int count = players.size();

        if(count >= map.getMinimumCapacity()) {
            state = ArenaState.COUNTDOWN;
        }
    }

    public void handleDisconnect(Player player) {
        players.remove(player);
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
}