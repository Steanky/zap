package io.github.zap.data;

import io.github.zap.serialize.DataSerializable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple data class used to represent a composite bounding box.
 */
public class MultiBoundingBox extends DataSerializable {
    private final Set<BoundingBox> boundingBoxes;

    public MultiBoundingBox() {
        boundingBoxes = new HashSet<>();
    }

    public MultiBoundingBox(Set<BoundingBox> boundingBoxes) {
        this.boundingBoxes = boundingBoxes;
    }

    /**
     * Adds a bounding box to the internal set.
     * @param bound The bounding box to add
     */
    public void addBoundingBox(BoundingBox bound) {
        boundingBoxes.add(bound);
    }

    /**
     * Runs BoundingBox#contains on all bounding boxes managed by this instance. If any of the boxes contain the
     * specified coordinate, it returns true.
     * @param coordinate The coordinate to test for
     * @return Whether or not any of the bounding boxes contain the specified point
     */
    public boolean anyContains(Vector coordinate) {
        for(BoundingBox bound : boundingBoxes) {
            if(bound.contains(coordinate)) {
                return true;
            }
        }

        return false;
    }
}