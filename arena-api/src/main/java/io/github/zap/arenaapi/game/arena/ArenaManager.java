package io.github.zap.arenaapi.game.arena;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.stats.StatsManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Generic interface for an ArenaManager.
 * @param <T> The type of arena this instance manages
 */
@RequiredArgsConstructor
public abstract class ArenaManager<T extends Arena<T>> implements Disposable {
    @Getter
    private final String gameName;

    @Getter
    private final Location hubLocation;

    @Getter
    protected final StatsManager statsManager;

    protected Map<UUID, T> managedArenas = new HashMap<>();

    @Getter
    private final Event<Arena<T>> arenaCreated = new Event<>();

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
    public abstract void handleJoin(JoinInformation joinAttempt, Consumer<Pair<Boolean, String>> onCompletion);

    /**
     * Whether or not this manager accepts players. Can be used to effectively "turn off" an ArenaManager.
     * @return True if this manager can accept join requests at the current time; false otherwise
     */
    public abstract boolean acceptsPlayers();

    /**
     * Removes the specified arena, after performing necessary shutdown tasks such as unloading worlds (if it can be
     * done without impacting other arenas).
     * @param arena The arena to remove
     */
    public abstract void unloadArena(T arena);

    public abstract boolean hasMap(String mapName);

    /**
     * Returns a read-only view of the Arena instances managed by this ArenaManager.
     * @return a read-only view of the Arena instances managed by this ArenaManager
     */
    public Map<UUID, Arena<T>> getArenas() {
        return Collections.unmodifiableMap(managedArenas);
    }

    @Override
    public void dispose() {
        statsManager.destroy();
    }

}
