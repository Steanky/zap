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

    private boolean contains(BoundingBox test, List<BoundingBox> sampleList) {
        for(int i = sampleList.size() - 1; i >= 0; i--) {
            BoundingBox sample = sampleList.get(i);

            if(sample.contains(test)) { //simplest case; our box definitely fits
                return true;
            }
            else if(sample.overlaps(test)) { //we must check further
                Vector diffs;

                Vector sampleEnd;
                Vector testStart;
                Vector testEnd;

                boolean xIn;
                boolean yIn;
                boolean zIn;

                if(sample.contains(test.getMin())) { //test max is out of bounds; determine how much
                    sampleEnd = sample.getMax();
                    testStart = test.getMin();
                    testEnd = test.getMax();

                    diffs = testEnd.clone().subtract(sampleEnd);

                    /*
                    to be considered 'inside' for a particular axis, diff must be < 0 since bounding boxes do not
                    consider vertices that exactly == maxValue to be inside.
                     */
                    xIn = diffs.getX() < 0;
                    yIn = diffs.getY() < 0;
                    zIn = diffs.getZ() < 0;
                }
                else if(sample.contains(test.getMax())) { //test min is out of bounds
                    sampleEnd = sample.getMin();
                    testStart = test.getMax();
                    testEnd = test.getMin();

                    diffs = sampleEnd.clone().subtract(testEnd);

                    //for min comparison, if diff == 0, we are inside — no need to test adjacent bounds
                    xIn = diffs.getX() <= 0;
                    yIn = diffs.getY() <= 0;
                    zIn = diffs.getZ() <= 0;
                }
                else { //sample contains neither corner, but may be needed in later check, so don't remove it
                    continue;
                }

                //don't iterate the sample that triggered this recursive check anymore
                sampleList.remove(i);

                /*
                very readable code below. creates and tests up to 3 new bounds that are non-overlapping slices of the
                original test bounds. does not execute a recursive call for 'slices' that are entirely located within
                the sample bounds, which is the purpose of xIn, yIn, and zIn + short circuit or.

                furthermore, the x-facing bounding box is the largest, followed by the y-facing, followed by the
                z-facing. the largest is tested first as it is the most likely to not fit, thus preventing additional
                recursive calls.
                 */
                return (xIn || contains(BoundingBox.of(new Vector(sampleEnd.getX(), testStart.getY(), testStart.getZ()), testEnd), sampleList)) &&
                        (yIn || contains(BoundingBox.of(new Vector(testStart.getX(), sampleEnd.getY(), testStart.getZ()), new Vector(sampleEnd.getX(), testEnd.getY(), testEnd.getZ())), sampleList)) &&
                        (zIn || contains(BoundingBox.of(new Vector(testStart.getX(), testStart.getY(), sampleEnd.getZ()), new Vector(sampleEnd.getX(), sampleEnd.getY(), testEnd.getZ())), sampleList));
            }
            else { //we don't need to keep samples that don't at least overlap in the list; may speed up things a bit
                sampleList.remove(i);
            }
        }

        return false;
    }

    /**
     * Uses a recursive algorithm to determine if the given BoundingBox is contained within the bounds specified by this
     * MultiBoundingBox.
     * @param test The BoundingBox to test
     * @return Whether or not test is entirely contained within the MultiBoundingBox, with no overlapping portions.
     */
    public boolean contains(BoundingBox test) {
        return contains(test, new ArrayList<>(boundingBoxes));
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
