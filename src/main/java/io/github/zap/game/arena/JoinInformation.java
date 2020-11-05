package io.github.zap.game.arena;

import lombok.Value;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

/**
 * This data class contains all the information for a 'join request'; that is, when a player attempts to join a game,
 * this object will represent that request.
 */
@Value
public class JoinInformation {
    /**
     * The players who are trying to join.
     */
    List<Player> players;

    /**
     * True if the players should be considered game spectators; false otherwise
     */
    boolean spectator;

    /**
     * The target map name. This may be null; in which case, targetArena must not be null.
     */
    String mapName;

    /**
     * The unique name of the arena to connect to, which should be ignored if mapName is not provided.
     */
    String targetArena;
}
