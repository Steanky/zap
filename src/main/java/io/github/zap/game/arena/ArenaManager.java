package io.github.zap.game.arena;

import io.github.zap.game.Unique;

import java.util.List;
import java.util.function.Consumer;

/**
 * Generic interface for an ArenaManager.
 * @param <T> The type of arena this instance manages
 */
public interface ArenaManager<T extends Arena> extends Unique {
    /**
     * Handle the specified JoinInformation. This method should create arenas as necessary to handle join requests.
     * This method may run fully or partially async, in which case it may return before the arena is created.
     *
     * This function must also take into account the possibility that some players in JoinInformation are offline.
     * Offline players must not be sent to the arena.
     * @param joinAttempt The JoinInformation object
     * @param onCompletion The consumer that will execute when the JoinInformation is processed. This should always
     *                     run on the main server thread.
     */
    void handleJoin(JoinInformation joinAttempt, Consumer<Boolean> onCompletion);

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
