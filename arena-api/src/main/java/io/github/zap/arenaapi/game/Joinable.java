package io.github.zap.arenaapi.game;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * Interface for classes containing players (parties, etc)
 */
public interface Joinable {
    /**
     * Whether or not this Joinable is valid.
     * @return True if valid, false if otherwise
     */
    boolean validate();

    /**
     * Gets the players contained in this instance.
     * @return The players contained in this instance
     */
    List<Player> getPlayers();
}
