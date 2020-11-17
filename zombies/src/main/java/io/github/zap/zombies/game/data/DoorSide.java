package io.github.zap.zombies.game.data;

import io.github.zap.arenaapi.game.MultiBoundingBox;
import io.github.zap.arenaapi.serialize.DataSerializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Represents a DoorSide, which can be defined as "a place from which the door may be opened".
 */
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoorSide extends DataSerializable {
    /**
     * What it will cost to open the door from this side.
     */
    int cost;

    /**
     * The names of the rooms this DoorSide will open, when it is purchased.
     */
    List<String> opensTo;

    /**
     * The bounds in which the player must stand in order to open the door from this side.
     */
    MultiBoundingBox triggerBounds;

    private DoorSide() {}
}
