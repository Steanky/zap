package io.github.zap.game;

import io.github.zap.serialize.DataSerializable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MultiBoundingBox extends DataSerializable {
    private final List<BoundingBox> boundingBoxes = new ArrayList<>();

    public void addBounds(BoundingBox boundingBox) {
        boundingBoxes.add(boundingBox);
    }

    /**
     * Checks if the provided vector is inside any of the bounds.
     * @param vector The vector to check
     * @return True if the provided vector is in every bounding box; false otherwise
     */
    public boolean contains(Vector vector) {
        for(BoundingBox boundingBox : boundingBoxes) {
            if(boundingBox.contains(vector)) {
                return true;
            }
        }

        return false;
    }
}
