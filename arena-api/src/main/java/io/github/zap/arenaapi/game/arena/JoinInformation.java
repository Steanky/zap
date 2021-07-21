package io.github.zap.arenaapi.game.arena;

import io.github.zap.arenaapi.game.Joinable;
import io.github.zap.arenaapi.game.Metadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * This data class contains all the information for a 'join request'; that is, when a player attempts to join a game,
 * this object will represent that request.
 */
public record JoinInformation(@NotNull Joinable joinable, @NotNull String gameName, @Nullable String mapName,
                              @Nullable UUID targetArena, @NotNull Metadata metadata) {

}
