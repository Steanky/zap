package io.github.zap.arenaapi.game.arena;

import org.bukkit.entity.Player;

public interface ManagedPlayerBuilder<T extends ManagedPlayer<T, V>, V extends ManagingArena<V, T>> {
    /**
     * Creates a managed player from an arena and a Bukkit player.
     * @param arena The arena
     * @param player The player
     * @return The managed player
     */
    T wrapPlayer(V arena, Player player);
}
