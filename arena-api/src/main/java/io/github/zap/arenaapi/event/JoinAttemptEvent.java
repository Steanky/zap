package io.github.zap.arenaapi.event;

import io.github.zap.arenaapi.game.arena.JoinInformation;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = false)
@Value
public class JoinAttemptEvent extends CustomEvent {
    JoinInformation information;
}
