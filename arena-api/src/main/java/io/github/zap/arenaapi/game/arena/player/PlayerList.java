package io.github.zap.arenaapi.game.arena.player;

import io.github.zap.arenaapi.Disposable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A list of players in an arena
 * @param <P> The type of player this list handles
 */
public interface PlayerList<P extends ManagedPlayer> extends Disposable {

    /**
     * Gets whether any new players can be added
     * @return Whether new players can be added
     */
    default boolean canPlayersJoin() {
        return true;
    }

    /**
     * Gets whether any players can rejoin
     * @return Whether any players can rejoin
     */
    default boolean canPlayersRejoin() {
        return true;
    }

    /**
     * Gets whether all players in a list can join
     * @param players The players to check
     * @return Whether all of the players can join
     */
    default boolean canAllPlayersJoin(@NotNull List<@NotNull Player> players) {
        return canPlayersJoin();
    }

    /**
     * Gets whether a player can join
     * @param player The player to check
     * @return Whether the player can join
     */
    default boolean canPlayerJoin(@NotNull Player player) {
        return !hasPlayer(player.getUniqueId()) && canAllPlayersJoin(Collections.singletonList(player));
    }

    /**
     * Gets whether all players in a list can rejoin
     * @param players The players to check
     * @return Whether all of the players can rejoin
     */
    default boolean canAllPlayersRejoin(@NotNull List<@NotNull P> players) {
        if (!canPlayersRejoin()) {
            return false;
        }

        Map<@NotNull UUID, @NotNull P> playerMap = getPlayerMap();
        for (@NotNull P player : players) {
            if (!playerMap.containsKey(player.getId())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets whether a player can rejoin
     * @param player The player to check
     * @return Whether the player can rejoin
     */
    default boolean canPlayerRejoin(@NotNull P player) {
        return canAllPlayersRejoin(Collections.singletonList(player));
    }

    /**
     * Gets a map of uuids to their respective managed players
     * @return The managed player map
     */
    @NotNull Map<@NotNull UUID, @NotNull P> getPlayerMap();

    default @NotNull Collection<P> getPlayers() {
        return List.copyOf(getPlayerMap().values());
    }

    default @NotNull Collection<P> getOnlinePlayers() {
        List<@NotNull P> players = new ArrayList<>();
        for (@NotNull P player : getPlayerMap().values()) {
            if (player.isInGame()) {
                players.add(player);
            }
        }

        return Collections.unmodifiableList(players);
    }

    /**
     * Gets a managed player by the player itself
     * @param player The player to get the managed player of
     * @return The managed player, or null if there is no matching managed player
     */
    default @Nullable P getPlayer(@NotNull Player player) {
        return getPlayer(player.getUniqueId());
    }

    /**
     * Gets a managed player by their id
     * @param id The unique id of the managed player
     * @return The managed player, or null if there is no matching managed player
     */
    default @Nullable P getPlayer(@NotNull UUID id) {
        return getPlayerMap().get(id);
    }

    /**
     * Gets a managed player by the player itself if they are online
     * @param player The player to get the managed player of
     * @return The managed player, or null if there is no matching online managed player
     */
    default @Nullable P getOnlinePlayer(@NotNull Player player) {
        return getOnlinePlayer(player.getUniqueId());
    }

    /**
     * Gets a managed player by their id if they are online
     * @param id The unique id of the managed player
     * @return The managed player, or null if there is no matching online managed player
     */
    default @Nullable P getOnlinePlayer(@NotNull UUID id) {
        P managedPlayer = getPlayer(id);
        if (managedPlayer == null) {
            return null;
        }
        if (managedPlayer.isOnline()) {
            return managedPlayer;
        }

        return null;
    }

    /**
     * Determines if this list has a player
     * @param player The player to check
     * @return Whether the list has a player
     */
    default boolean hasPlayer(@NotNull Player player) {
        return hasPlayer(player.getUniqueId());
    }

    /**
     * Determines if this list has a player
     * @param id The id of the player to check
     * @return Whether the list has a player
     */
    default boolean hasPlayer(@NotNull UUID id) {
        return getPlayerMap().containsKey(id);
    }

    /**
     * Adds a player from the list if this list does not manage it
     * @param player The player to add
     * @return Whether the player add was successful
     */
    boolean addPlayer(@NotNull Player player);

    /**
     * Removes a player from the list if this list manages it
     * @param id The id of the player to remove
     * @return Whether the removal was successful
     */
    default boolean removePlayer(@NotNull UUID id) {
        P player = getPlayerMap().get(id);
        if (player != null) {
            return removePlayer(player);
        }

        return false;
    }

    /**
     * Removes a player from the list if this list manages it
     * @param player The player to remove
     * @return Whether the removal was successful
     */
    boolean removePlayer(@NotNull P player);

}
