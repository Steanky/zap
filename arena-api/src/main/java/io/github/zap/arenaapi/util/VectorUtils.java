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
    public static Vector[] interpolateLine(Vector start, Vector end, int density) {
        double distance = start.distance(end);

        Vector startClone = start.clone();
        Vector endClone = end.clone();

        endClone.add(startClone.clone().multiply(-1)); //end is now a vector with the same angle as the vector formed from s to e

        int particles = (int)Math.round(distance * density);
        Vector[] vectors = new Vector[particles];
        endClone.multiply(1D / particles); //end is now a vector with a length equaling the distance between particles

        for(int i = 0; i < particles; i++) {
            vectors[i] = startClone.clone();
            startClone.add(endClone);
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
        Vector origin = box.getMin();
        Vector limit = box.getMax().add(UNIT);

        Vector one = new Vector(origin.getX(), origin.getY(), limit.getZ());
        Vector two = new Vector(limit.getX(), origin.getY(), limit.getZ());
        Vector three = new Vector(limit.getX(), origin.getY(), origin.getZ());
        Vector four = new Vector(origin.getX(), limit.getY(), origin.getZ());
        Vector five = new Vector(origin.getX(), limit.getY(), limit.getZ());
        Vector seven = new Vector(limit.getX(), limit.getY(), origin.getZ());

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
