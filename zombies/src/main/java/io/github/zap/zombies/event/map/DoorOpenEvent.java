package io.github.zap.zombies.event.map;

import io.github.zap.arenaapi.event.CustomEvent;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.DoorData;
import io.github.zap.zombies.game.data.DoorSide;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Value
public class DoorOpenEvent extends CustomEvent {
    ZombiesPlayer player;
    DoorData openedDoor;
    DoorSide openedSide;
}
