package io.github.zap.game.manager;

import lombok.Value;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * This data class contains all the information for a 'join request'; that is, when a player attempts to join a game,
 * this object will represent that request.
 */
@Value
public class JoinInformation {
    /*
    Use a set of players so we can support parties
     */
    Set<Player> players;

    /*
    Add other fields here later
     */
}
