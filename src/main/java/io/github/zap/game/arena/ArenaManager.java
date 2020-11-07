package io.github.zap.game.arena;

import java.util.List;

/**
 * Generic interface for an ArenaManager.
 * @param <T> The type of arena this instance manages
 */
public interface ArenaManager<T extends Arena> {
    /**
     * Handle the specified JoinInformation.
     * @param joinAttempt The JoinInformation object
     * @return This method should return false if the requested operation succeeded (ex. all of the users joined the
     * game)
     */
    boolean handleJoin(JoinInformation joinAttempt);

    /**
     * Removes the specified arena from the manager.
     * @param name The name of the arena to remove
     */
    void removeArena(String name);

    /**
     * Retrieves an arena from the internal map.
     * @param name The name of the arena to retrieve
     * @return The arena itself
     */
    T getArena(String name);

    /**
     * Returns a list of arenas managed by this ArenaManager.
     * @return A list of the arenas managed by this ArenaManager. This should be a copy of the underlying Collection,
     * so that illegal modifications cannot be performed
     */
    List<T> getArenas();
}
