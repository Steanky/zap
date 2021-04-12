package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.util.VectorUtils;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public interface WorldVectorSource extends BlockVectorSource {
    double worldX();

    double worldY();

    double worldZ();

    default double distanceSquared(@NotNull WorldVectorSource other) {
        return VectorUtils.distanceSquared(worldX(), worldY(), worldZ(), other.worldX(), other.worldY(), other.worldZ());
    }

    static WorldVectorSource fromWorldCoordinate(double x, double y, double z) {
        return new WorldVectorSourceImpl(x, y, z);
    }

    static WorldVectorSource fromWorldVector(@NotNull Vector vector) {
        return fromWorldCoordinate(vector.getX(), vector.getY(), vector.getZ());
    }
}
