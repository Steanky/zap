package io.github.zap.game;

import io.github.zap.game.data.MapData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class ZombiesArena implements Tickable, Listener {
    @Getter
    private final MapData map;

    @Getter
    private final World world;

    @Getter
    private final Set<Player> players = new HashSet<>();

    /**
     * Gets the name of this arena.
     * @return The name of this arena, which is the same as the world name used to create it
     */
    @Override
    public String getName() {
        return world.getName();
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

    }

    @Override
    public void doTick() {

    }
}
