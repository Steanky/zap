package io.github.zap.zombies.game.data;

import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.game.MultiBoundingBox;
import io.github.zap.arenaapi.serialize.DataSerializable;
import io.github.zap.arenaapi.serialize.NoSerialize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Represents a door.
 */
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoorData extends DataSerializable {
    /**
     * The bounds of the door, any of which may be right-clicked in attempt to open it
     */
    MultiBoundingBox doorBounds;

    /**
     * The list of DoorSide objects. Doors typically contain 2 sides but may contain any number
     */
    List<DoorSide> sides;

    /**
     * Arena-specific state information: whether or not the door is open
     */
    @NoSerialize
    final Property<Boolean> openAccessor = new Property<>(false);

    private DoorData() { }

    /**
     * Returns the DoorSide object whose trigger bounds contain the provided vector.
     * @param standingPosition The vector that may be inside a trigger bounds
     * @return The corresponding DoorSide object, or null if the provided vector is not inside of a trigger
     */
    public DoorSide sideAt(Vector standingPosition) {
        for(DoorSide side : sides) {
            if(side.getTriggerBounds().contains(standingPosition)) {
                return side;
            }
        }

        return null;
    }
}
