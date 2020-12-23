package io.github.zap.zombies.game.data;

import io.github.zap.arenaapi.game.MultiBoundingBox;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a DoorSide, which can be defined as "a place from which the door may be opened".
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoorSide {
    /**
     * What it will cost to open the door from this side.
     */
    int cost = 69420;

    /**
     * The names of the rooms this DoorSide will open, when it is purchased.
     */
    List<String> opensTo = new ArrayList<>();

    /**
     * The bounds in which the player must stand in order to open the door from this side.
     */
    MultiBoundingBox triggerBounds = new MultiBoundingBox();

    public DoorSide() {}
}
