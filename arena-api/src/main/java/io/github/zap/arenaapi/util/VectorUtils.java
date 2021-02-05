package io.github.zap.arenaapi.util;

import io.github.zap.arenaapi.game.MultiBoundingBox;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public final class VectorUtils {
    public static final Vector[] EMPTY_VECTOR_ARRAY = new Vector[0];

    private static final Vector UNIT = new Vector(1, 1, 1);

    /**
     * Calculates the Manhattan (taxicab) distance between two vectors. Created by taking the sum of the absolute values
     * of the distances between each input vector's X, Y, and Z values.
     * @param first The first vector
     * @param second The second vector
     * @return The Manhattan distance between the two vectors
     */
    public static double manhattanDistance(Vector first, Vector second) {
        return Math.abs(first.getX() - second.getX()) + Math.abs(first.getY() - second.getY()) +
                Math.abs(first.getZ() - second.getZ());
    }

    /**
     * Produces an array of vectors along the 3-dimensional line specified by the given vectors.
     * @param start The starting vector
     * @param end The ending vector
     * @param density The interpolation density, measured in particles per block (PPB)
     * @return An array of vectors created by interpolating along the line bounded by start and end
     */
    public static Vector[] interpolateLine(ImmutableVector start, ImmutableVector end, int density) {
        double distance = start.distance(end);

        end = start.add(end.multiply(-1));

        int particles = (int)Math.round(distance * density);
        Vector[] vectors = new Vector[particles];
        end = end.multiply(1D / particles);

        for(int i = 0; i < particles; i++) {
            vectors[i] = start.toBukkitVector();
            start = start.add(end);
        }

        return vectors;
    }

    /**
     * Returns an array of Vectors representing the result of interpolating each 3d line that comprises an edge of this
     * BoundingBox.
     * @param box The input bounding box
     * @param density The density of the particles, measured in PPB (particles per block)
     * @return
     */
    public static Vector[] interpolateBounds(BoundingBox box, int density) {
        ImmutableVector origin = new ImmutableVector(box.getMin());
        ImmutableVector limit = new ImmutableVector(box.getMax().add(UNIT));

        ImmutableVector one = new ImmutableVector(origin.x, origin.y, limit.z);
        ImmutableVector two = new ImmutableVector(limit.x, origin.y, limit.z);
        ImmutableVector three = new ImmutableVector(limit.x, origin.y, origin.z);
        ImmutableVector four = new ImmutableVector(origin.x, limit.y, origin.z);
        ImmutableVector five = new ImmutableVector(origin.x, limit.y, limit.z);
        ImmutableVector seven = new ImmutableVector(limit.x, limit.y, origin.z);

        return ArrayUtils.combine(interpolateLine(origin, one, density), interpolateLine(one, two, density),
                interpolateLine(two, three, density), interpolateLine(three, origin, density),
                interpolateLine(origin, four, density), interpolateLine(one, five, density),
                interpolateLine(two, limit, density), interpolateLine(three, seven, density),
                interpolateLine(four, five, density), interpolateLine(five, limit, density),
                interpolateLine(limit, seven, density), interpolateLine(seven, four, density));
    }

    public static Vector[] interpolateBounds(MultiBoundingBox box, int density) {
        List<Vector[]> boundsVectors = new ArrayList<>(box.getBounds().size());

        for(BoundingBox bounds : box.getBounds()) {
            Vector[] vectors = interpolateBounds(bounds, density);
            boundsVectors.add(vectors);
        }

        return ArrayUtils.combine(boundsVectors, Vector.class);
    }
}
