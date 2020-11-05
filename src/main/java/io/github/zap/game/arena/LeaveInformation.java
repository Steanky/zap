package io.github.zap.game.arena;

import lombok.Value;
import org.bukkit.entity.Player;

import java.util.List;

@Value
public class LeaveInformation {
    List<Player> players;
    boolean spectator;
}
