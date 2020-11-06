package io.github.zap.game.arena;

import lombok.Value;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Data class representing a player's attempt to leave an arena.
 */
@Value
public class LeaveInformation {
    /**
     * The players trying to leave
     */
    List<Player> players;

    /**
     * Whether or not those players are spectators
     */
    boolean spectator;
}
