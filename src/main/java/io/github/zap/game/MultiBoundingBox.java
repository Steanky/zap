package io.github.zap.game;

import io.github.zap.serialize.DataSerializable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class MultiBoundingBox extends DataSerializable {
    private final List<BoundingBox> boundingBoxes = new ArrayList<>();

    /**
     * Adds a BoundingBox to this instance.
     * @param boundingBox The BoundingBox to include
     */
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

    public List<BoundingBox> getBounds() {
        return new ArrayList<>(boundingBoxes);
    }
}
