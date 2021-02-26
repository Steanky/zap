package io.github.zap.arenaapi.util;

import io.github.zap.arenaapi.game.MultiBoundingBox;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

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
}
