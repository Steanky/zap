package io.github.zap.manager;

import lombok.Value;
import org.bukkit.entity.Player;

import java.util.Set;

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
