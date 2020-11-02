package io.github.zap.game.mapdata;

import io.github.zap.game.MultiBoundingBox;
import io.github.zap.serialize.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.util.Vector;

import java.util.Set;

@AllArgsConstructor
public class DoorData extends DataSerializable {
    @Getter
    private MultiBoundingBox doorBounds;

    @Getter
    private Set<DoorSide> sides;

    private DoorData() {}

    public DoorSide tryOpen(Vector standingVector, Vector clickVector) {
        if(doorBounds.contains(clickVector)) {
            for(DoorSide side : sides) {
                if(side.insideTrigger(standingVector)) {
                    return side;
                }
            }
        }

        return null;
    }
}
