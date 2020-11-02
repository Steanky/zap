package io.github.zap.game.mapdata;

import io.github.zap.game.MultiBoundingBox;
import io.github.zap.serialize.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.util.Vector;

import java.util.Set;

@AllArgsConstructor
public class DoorSide extends DataSerializable {
    @Getter
    private int cost;

    @Getter
    private Set<String> openings;

    private MultiBoundingBox triggerBounds;

    private DoorSide() {}

    public boolean insideTrigger(Vector vector) {
        return triggerBounds.contains(vector);
    }
}
