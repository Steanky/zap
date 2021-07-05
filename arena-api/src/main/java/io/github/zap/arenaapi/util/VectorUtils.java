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

    public static double distanceSquared(double x1, double y1, double z1, double x2, double y2, double z2) {
        double xD = x1 - x2;
        double yD = y1 - y2;
        double zD = z1 - z2;

        return (xD * xD) + (yD * yD) + (zD * zD);
    }

    public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(distanceSquared(x1, y1, z1, x2, y2, z2));
    }

    public static int manhattan(int x1, int y1, int z1, int x2, int y2, int z2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2) + Math.abs(z1 - z2);
    }
}
