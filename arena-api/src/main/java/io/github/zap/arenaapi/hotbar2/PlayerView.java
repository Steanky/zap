package io.github.zap.arenaapi.hotbar2;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * Object that may hold a safely cached player reference. It should be fine to store implementations of this interface
 * long-term.
 *
 * These should be used in preference of Player when passing to functions and constructors.
 */
public interface PlayerView {
    /**
     * Returns an {@link Optional} that may contain the {@link Player} this PlayerView object handles. If the optional
     * is not empty, the player must be online. However, implementations may define additional cases in which players
     * are considered to be "invalid" and may return an empty Optional even if the player is online.
     * @return An Optional, which must be empty if the player is offline
     */
    @NotNull Optional<Player> getPlayerIfValid();

    /**
     * Gets an {@link OfflinePlayer} instance for this PlayerView. Note that this may actually be a {@link Player}
     * object if the player in question is currently online.
     * @return An OfflinePlayer instance
     */
    @NotNull OfflinePlayer getOfflinePlayer();

    /**
     * Gets the {@link UUID} for the player referred to by this PlayerView.
     * @return The player's UUID
     */
    @NotNull UUID getUUID();
}
