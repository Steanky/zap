package io.github.zap.data;

import io.github.zap.serialize.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.util.Vector;

import java.util.List;

@AllArgsConstructor
@Getter
public class DoorData extends DataSerializable {
    private MultiBoundingBox bounds;
    private List<MultiBoundingBox> sides;
    private List<Integer> costs;

    private DoorData() {}

    /**
     * Gets the index of the side that the specified vector is inside.
     * @param coordinate The vector to test
     * @return The index of the side that the specified vector is located, or -1 if the vector is outside of all sides
     */
    public int getSideAt(Vector coordinate) {
        for(int i = 0; i < sides.size(); i++) {
            if(sides.get(i).anyContains(coordinate)) {
                return i;
            }
        }

        return -1;
    }
}
