package io.github.zap.game;

import lombok.Value;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * This data class contains all the information for a 'join request'; that is, when a player attempts to join a game,
 * this object will represent that request.
 */
@Value
public class JoinInformation {
    Set<Player> players;

    String mapName;

    boolean asSpectator;
}
