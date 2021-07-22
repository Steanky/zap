package io.github.zap.arenaapi.game.arena;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.stats.StatsManager;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Manager of Arenas of a specific type. Routes players to and from arenas.
 * @param <T> The type of arena this instance manages
 */
public abstract class ArenaManager<T extends Arena<T>> implements Disposable {

    private final String gameName;

    private final Location hubLocation;

    protected final StatsManager statsManager;

    protected Map<@NotNull UUID, @NotNull T> managedArenas = new HashMap<>();

    private final Event<@NotNull Arena<@NotNull T>> arenaCreated = new Event<>();

    public ArenaManager(@NotNull String gameName, @NotNull Location hubLocation, @NotNull StatsManager statsManager) {
        this.gameName = gameName;
        this.hubLocation = hubLocation;
        this.statsManager = statsManager;
    }

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
    public abstract void handleJoin(@NotNull JoinInformation joinAttempt,
                                    @NotNull Consumer<Pair<Boolean, String>> onCompletion);

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
     * Gets a map of the UUIDs of arenas which manage a certain player
     * @param player The player to get the arenas for
     * @return A list of the UUIDs of arenas which manage the player
     */
    public @NotNull Map<@NotNull UUID, @NotNull T> getArenasWithPlayer(@NotNull Player player) {
        Map<UUID, T> arenaMap = new HashMap<>();
        for (Map.Entry<UUID, T> managedArena : managedArenas.entrySet()) {
            if (managedArena.getValue().hasPlayer(player.getUniqueId())) {
                arenaMap.put(managedArena.getKey(), managedArena.getValue());
            }
        }

        return arenaMap;
    }

    /**
     * Returns a read-only view of the Arena instances managed by this ArenaManager.
     * @return a read-only view of the Arena instances managed by this ArenaManager
     */
    public Map<@NotNull UUID, @NotNull Arena<@NotNull T>> getArenas() {
        return Collections.unmodifiableMap(managedArenas);
    }

    @Override
    public void dispose() {
        statsManager.destroy();
        for (T arena : managedArenas.values()) {
            arena.dispose();
        }
    }

    /**
     * Gets the name of the game that this ArenaManager manages
     * @return The game name
     */
    public @NotNull String getGameName() {
        return gameName;
    }

    /**
     * Gets the location to warp players to after an arena disposes
     * If the world in the location is unloaded, players should be kicked from the server
     * @return The location
     */
    public @NotNull Location getHubLocation() {
        return hubLocation;
    }

    /**
     * Gets the shared {@link StatsManager} for this arena manager
     * @return The stats manager
     */
    public @NotNull StatsManager getStatsManager() {
        return statsManager;
    }

    /**
     * Gets the event associated with an arena being created
     * @return The arena creation event
     */
    public @NotNull Event<@NotNull Arena<@NotNull T>> getArenaCreated() {
        return arenaCreated;
    }

}
