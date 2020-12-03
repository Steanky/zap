package io.github.zap.arenaapi.game.arena;

import lombok.Value;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * This data class contains all the information for a 'join request'; that is, when a player attempts to join a game,
 * this object will represent that request.
 */
@Value
public class JoinInformation {
    int leadPlayer;
    Player[] players;
    boolean spectator;
    String gameName;
    String mapName;
    UUID targetArena;
}
