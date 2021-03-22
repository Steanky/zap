package io.github.zap.arenaapi.game;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Encapsulates a composite bounded area made up of any number of axis-aligned bounding boxes, which are not required
 * to be continuous and may also overlap.
 */
public class MultiBoundingBox implements Iterable<BoundingBox> {
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

    public boolean contains(double x, double y, double z) {
        for(BoundingBox boundingBox : boundingBoxes) {
            if(boundingBox.contains(x, y, z)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if all bounds for this MultiBoundingBox are contained in the given BoundingBox. Returns false
     * otherwise
     * @param other The bounds to check
     * @return True if this MultiBoundingBox is inside the given bounds; false otherwise
     */
    public boolean inside(BoundingBox other) {
        for(BoundingBox boundingBox : boundingBoxes) {
            if(!other.contains(boundingBox)) {
                return false;
            }
        }

        return true;
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
                }
                else if(sample.contains(test.getMax())) { //test min is out of bounds
                    sampleEnd = sample.getMin();
                    testStart = test.getMax();
                    testEnd = test.getMin();

                    diffs = sampleEnd.clone().subtract(testEnd);
                }
                else { //sample contains neither corner, but may be needed in later check, so don't remove it
                    continue;
                }

                xIn = diffs.getX() <= 0.0D;
                yIn = diffs.getY() <= 0.0D;
                zIn = diffs.getZ() <= 0.0D;

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
     * Tests if the given BoundingBox overlaps any of the individual bounds contained in this MultiBoundingBox instance.
     * @param test The BoundingBox to test for overlaps
     * @return Whether or not test overlaps with any bounds contained by this instance
     */
    public boolean overlaps(BoundingBox test) {
        for(BoundingBox boundingBox : boundingBoxes) {
            if(boundingBox.overlaps(test)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the underlying list for this BoundingBox.
     * @return The underlying list
     */
    public List<BoundingBox> getList() {
        return boundingBoxes;
    }

    /**
     * Gets the number of BoundingBoxes contained in this instance.
     * @return The size of this MultiBoundingBox
     */
    public int size() {
        return boundingBoxes.size();
    }

    /**
     * Returns the BoundingBox at the specified index.
     * @param i The index
     * @return The BoundingBox at index i
     */
    public BoundingBox get(int i) {
        return boundingBoxes.get(i);
    }

    /**
     * Sets the bounds at the specified index.
     * @param i The index to set
     * @param bounds The bounds to set at the index
     */
    public void set(int i, BoundingBox bounds) {
        boundingBoxes.add(i, bounds);
    }

    /**
     * Removes the BoundingBox at the specified index
     * @param i The index to remove at
     */
    public void remove(int i) {
        boundingBoxes.remove(i);
    }

    @NotNull
    @Override
    public Iterator<BoundingBox> iterator() {
        return boundingBoxes.iterator();
    }
}
