package io.github.zap.arenaapi.game.arena;

import io.github.zap.arenaapi.game.Joinable;
import io.github.zap.arenaapi.game.Metadata;
import lombok.Value;

import java.util.UUID;

/**
 * This data class contains all the information for a 'join request'; that is, when a player attempts to join a game,
 * this object will represent that request.
 */
@Value
public class JoinInformation {
    Joinable joinable;
    String gameName;
    String mapName;
    UUID targetArena;
    Metadata metadata;
}
