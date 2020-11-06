package io.github.zap.event.map;

import io.github.zap.event.CustomEvent;
import io.github.zap.game.arena.ZombiesPlayer;
import io.github.zap.game.data.DoorData;
import io.github.zap.game.data.DoorSide;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DoorOpenEvent extends CustomEvent {
    ZombiesPlayer player;
    DoorData openedDoor;
    DoorSide openedSide;
    List<String> openedRoomNames;
}
