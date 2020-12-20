package io.github.zap.arenaapi.game.arena;

import io.github.zap.arenaapi.game.Joinable;
import io.github.zap.arenaapi.game.Metadata;
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
    Joinable joinable;
    UUID targetWorld;
    Metadata metadata;
}
