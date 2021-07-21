package io.github.zap.arenaapi.game.arena.player;

import io.github.zap.arenaapi.game.arena.Arena;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for creating managed players that wrap Bukkit players (for use in {@link Arena}s)
 * @param <T> The ManagedPlayer subclass type
 */
@FunctionalInterface
public interface ManagedPlayerBuilder<T extends ManagedPlayer> {

    /**
     * Creates a managed player from a Bukkit player.
     * @param player The player
     * @return The managed player
     */
    @NotNull T wrapPlayer(@NotNull Player player);

}
