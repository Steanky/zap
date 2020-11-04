package io.github.zap.event;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Set;

@RequiredArgsConstructor
public class PlayerJoinEvent extends CustomEvent {
    private final Set<Player> players;
    private final boolean spectator;
}
