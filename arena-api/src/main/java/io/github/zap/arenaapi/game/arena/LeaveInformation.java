package io.github.zap.arenaapi.game.arena;

import lombok.Value;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Data class representing a player's attempt to leave an arena.
 */
@Value
public class LeaveInformation {
    UUID leader;
    Set<UUID> players;
    boolean spectator;
    UUID targetLobby;
}
