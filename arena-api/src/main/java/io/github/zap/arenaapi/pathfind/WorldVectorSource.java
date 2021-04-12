package io.github.zap.arenaapi.pathfind;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public interface WorldVectorSource extends BlockVectorSource {
    double worldX();

    double worldY();

    double worldZ();

    static WorldVectorSource fromWorldCoordinate(double x, double y, double z) {
        return new WorldVectorSource() {
            @Override
            public double worldX() {
                return x;
            }

            @Override
            public double worldY() {
                return y;
            }

            @Override
            public double worldZ() {
                return x;
            }

            @Override
            public int blockX() {
                return (int)x;
            }

            @Override
            public int blockY() {
                return (int)y;
            }

            @Override
            public int blockZ() {
                return (int)z;
            }
        };
    }

    static WorldVectorSource fromWorldVector(@NotNull Vector vector) {
        return fromWorldCoordinate(vector.getX(), vector.getY(), vector.getZ());
    }
}
