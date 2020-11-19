package io.github.zap.zombies.event.player;

import io.github.zap.arenaapi.event.CustomEvent;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Value
public class PlayerJoinArenaEvent extends CustomEvent {
    JoinInformation attempt;
}