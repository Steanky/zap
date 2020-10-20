package io.github.zap.data;

import io.github.zap.serialize.DataSerializable;
import lombok.Getter;
import org.bukkit.util.Vector;

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

    /**
     * Gets the index of the side that the specified vector is inside.
     * @param coordinate The vector to test
     * @return The index of the side that the specified vector is located, or -1 if the vector is outside of all sides
     */
    public int getSideAt(Vector coordinate) {
        for(int i = 0; i < sides.length; i++) {
            if(sides[i].anyContains(coordinate)) {
                return i;
            }
        }

        return -1;
    }
}
