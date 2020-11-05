package io.github.zap.event.player;

import io.github.zap.event.CustomEvent;
import io.github.zap.game.arena.ZombiesArena;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.entity.Player;

import java.util.List;

@Getter
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PlayerLeaveArenaEvent extends CustomEvent {
    ZombiesArena leftArena;
    List<Player> players;
    boolean spectator;
}
