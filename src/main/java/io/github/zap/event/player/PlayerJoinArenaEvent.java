package io.github.zap.event.player;

import io.github.zap.event.CustomEvent;
import io.github.zap.game.arena.Arena;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class PlayerJoinArenaEvent extends CustomEvent {
    private final Arena joinedArena;
    private final List<Player> players;
    private final boolean spectator;
}
