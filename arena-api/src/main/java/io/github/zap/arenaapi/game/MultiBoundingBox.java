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

    private boolean contains(BoundingBox test, int startingIndex) {
        for(int i = startingIndex; i < boundingBoxes.size(); i++) {
            BoundingBox sample = boundingBoxes.get(i);

            if(sample.contains(test)) { //simplest case; our box definitely fits
                return true;
            }
            else {
                Vector diffs;

                Vector sampleEnd;
                Vector testStart;
                Vector testEnd;

                boolean max;
                if(sample.contains(test.getMin())) { //test max is out of bounds
                    diffs = test.getMax().subtract(sample.getMax());

                    sampleEnd = sample.getMax();
                    testStart = test.getMin();
                    testEnd = test.getMax();

                    max = true;
                } else if(sample.contains(test.getMax())) { //test min is out of bounds
                    diffs = sample.getMin().subtract(test.getMin());

                    sampleEnd = sample.getMin(); //reverse everything so we can use the same variables
                    testStart = test.getMax();
                    testEnd = test.getMin();

                    max = false;
                }
                else {
                    continue;
                }

                boolean xIn;
                boolean yIn;
                boolean zIn;

                if(max) {
                    xIn = diffs.getX() < 0;
                    yIn = diffs.getY() < 0;
                    zIn = diffs.getZ() < 0;
                }
                else {
                    xIn = diffs.getX() <= 0;
                    yIn = diffs.getY() <= 0;
                    zIn = diffs.getZ() <= 0;
                }

                return (xIn || contains(BoundingBox.of(new Vector(sampleEnd.getX(), testStart.getY(), testStart.getZ()), testEnd), i + 1)) &&
                        (yIn || contains(BoundingBox.of(new Vector(testStart.getX(), sampleEnd.getY(), testStart.getZ()), new Vector(sampleEnd.getX(), testEnd.getY(), testEnd.getZ())), i + 1)) &&
                        (zIn || contains(BoundingBox.of(new Vector(testStart.getX(), testStart.getY(), sampleEnd.getZ()), new Vector(sampleEnd.getX(), sampleEnd.getY(), testEnd.getZ())), i + 1));
            }
        }

        return false;
    }

    /**
     * Determines if the provided bounding box is entirely contained within this MultiBoundingBox. Uses a recursive
     * algorithm to account for cases in which two (or more) bounding boxes are directly next to each other, with test
     * situated such that it can be found in both bounds at once.
     * @param test The BoundingBox to test
     * @return Whether or not this BoundingBox is entirely contained within this MultiBoundingBox.
     */
    public boolean contains(BoundingBox test) {
        return contains(test, 0);
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
