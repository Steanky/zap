package io.github.zap.arenaapi.game;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates a bounding box made up of any number of axis-aligned bounding boxes.
 */
public class MultiBoundingBox {
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
     * @return True if the provided vector is in any bounding box; false otherwise
     */
    public boolean contains(Vector vector) {
        for(BoundingBox boundingBox : boundingBoxes) {
            if(boundingBox.contains(vector)) {
                return true;
            }
        }

        return false;
    }

    public boolean contains(BoundingBox test) {
        for(BoundingBox sample : boundingBoxes) {
            if(sample.contains(test)) { //simplest case; we can stop testing
                return true;
            }
            else {

            }
        }
        return false;
    }

    /**
     * Returns copies of all the bounding boxes this instance contains. This method calls clone() on every bounding
     * box to ensure that its internal state is protected.
     * @return A copy of all the bounding boxes this instance contains
     */
    public List<BoundingBox> getBounds() {
        List<BoundingBox> result = new ArrayList<>();
        for(BoundingBox internalBounds : boundingBoxes) {
            result.add(internalBounds.clone());
        }

        return result;
    }
}
