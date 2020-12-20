package io.github.zap.arenaapi.game.arena;

import org.bukkit.entity.Player;

/**
 * Interface for creating managed players that wrap Bukkit players (for use in ManagingArenas)
 * @param <T> The ManagedPlayer subclass type
 * @param <V> The ManagingArena that handles this kind of player
 */
public interface ManagedPlayerBuilder<T extends ManagedPlayer<T, V>, V extends ManagingArena<V, T>> {
    /**
     * Creates a managed player from an arena and a Bukkit player.
     * @param arena The arena
     * @param player The player
     * @return The managed player
     */
    T wrapPlayer(V arena, Player player);
}
