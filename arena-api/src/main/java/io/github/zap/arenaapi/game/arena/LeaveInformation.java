package io.github.zap.arenaapi.game.arena;

import lombok.Value;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Data class representing a player's attempt to leave an arena.
 */
@Value
public class LeaveInformation {
    int leadPlayer;
    Player[] players;
    String targetLobby;
    boolean spectator;
}
