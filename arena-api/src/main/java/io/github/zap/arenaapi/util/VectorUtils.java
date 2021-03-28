package io.github.zap.arenaapi.util;

import org.bukkit.util.Vector;

public final class VectorUtils {
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

    public static int distanceSquared(int x1, int y1, int z1, int x2, int y2, int z2) {
        int xD = x1 - x2;
        int yD = y1 - y2;
        int zD = z1 - z2;

        return  (xD * xD) + (yD * yD) + (zD * zD);
    }
}
