package io.github.zap.arenaapi.game;

import org.apache.commons.lang3.tuple.Pair;
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
     * Gets the list of groups of players and their respective metadata
     * @return The groups contained in this instance
     */
    List<Pair<List<Player>, Metadata>> groups();
}
