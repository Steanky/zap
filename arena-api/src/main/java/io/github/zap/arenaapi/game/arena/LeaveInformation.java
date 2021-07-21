package io.github.zap.arenaapi.game.arena;

import io.github.zap.arenaapi.game.Joinable;
import io.github.zap.arenaapi.game.Metadata;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Data class representing a player's attempt to leave an arena.
 */
public record LeaveInformation(@NotNull Joinable joinable, @NotNull UUID sourceWorld, @NotNull Metadata metadata) {

}
