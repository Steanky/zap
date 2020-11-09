package io.github.zap.game.arena;

import io.github.zap.game.Unique;
import org.apache.commons.lang3.tuple.ImmutablePair;

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
     *                     run on the main server thread. The first parameter of the ImmutablePair should correspond
     *                     to the success of the operation (whether or not the players were added). The second
     *                     parameter should be a resource key pointing to an error message explaining, in user-friendly
     *                     terms, why the JoinAttempt was rejected. It should only be non-null if the first part of
     *                     the pair is false.
     */
    void handleJoin(JoinInformation joinAttempt, Consumer<ImmutablePair<Boolean, String>> onCompletion);

    /**
     * Removes the specified arena from the manager.
     * @param name The name of the arena to remove
     */
    void removeArena(String name);
}
