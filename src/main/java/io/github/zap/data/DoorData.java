package io.github.zap.data;

import io.github.zap.serialize.DataSerializable;
import lombok.Getter;

public class DoorData extends DataSerializable {
    @Getter
    private MultiBoundingBox bounds;

    @Getter
    private MultiBoundingBox[] sides;

    @Getter
    private int[] costs;

    private DoorData() {}

    public DoorData(MultiBoundingBox bounds, MultiBoundingBox[] sides, int[] costs) {
        this.bounds = bounds;
        this.sides = sides;
        this.costs = costs;
    }
}
