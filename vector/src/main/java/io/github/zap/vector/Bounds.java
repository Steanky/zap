package io.github.zap.vector;

import org.jetbrains.annotations.NotNull;

public class Bounds {
    private final double minX;
    private final double minY;
    private final double minZ;

    private final double maxX;
    private final double maxY;
    private final double maxZ;

    private Bounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, boolean ignored) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;

        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public Bounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this(Math.min(minX, maxX), Math.min(minY, maxY), Math.min(minZ, maxZ),
                Math.max(minX, maxX), Math.max(minY, maxY), Math.max(minZ, maxZ), false);
    }
}
