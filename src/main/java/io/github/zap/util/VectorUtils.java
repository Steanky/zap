package io.github.zap.util;

import org.bukkit.util.Vector;

public final class VectorUtils {
    public static double manhattanDistance(Vector first, Vector second) {
        double xD = Math.abs(first.getX() - second.getX());
        double yD = Math.abs(first.getY() - second.getY());
        double zD = Math.abs(first.getZ() - second.getZ());

        return xD + yD + zD;
    }
}
