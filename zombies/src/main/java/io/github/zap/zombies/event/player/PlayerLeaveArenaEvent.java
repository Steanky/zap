package io.github.zap.zombies.event.player;

import io.github.zap.arenaapi.event.CustomEvent;
import io.github.zap.arenaapi.game.arena.LeaveInformation;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Value
public class PlayerLeaveArenaEvent extends CustomEvent {
    LeaveInformation attempt;
}
