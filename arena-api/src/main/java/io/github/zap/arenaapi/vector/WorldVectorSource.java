package io.github.zap.arenaapi.vector;

import io.github.zap.arenaapi.util.VectorUtils;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WorldVectorSource extends BlockVectorSource {
    public static final double EPSILON = 1.0E-6D;

    private final double x;
    private final double y;
    private final double z;
    private int hash = -1;

    public WorldVectorSource(double x, double y, double z) {
        super((int)x, (int)y, (int)z);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public WorldVectorSource(@NotNull Vector vector) {
        super(vector);
        x = vector.getX();
        y = vector.getY();
        z = vector.getZ();
    }

    public double worldX() {
        return x;
    }

    public double worldY() {
        return y;
    }

    public double worldZ() {
        return z;
    }

    public double distanceSquared(@NotNull WorldVectorSource other) {
        return VectorUtils.distanceSquared(x, y, z, other.x, other.y, other.z);
    }

    public double distance(@NotNull WorldVectorSource other) {
        return VectorUtils.distance(x, y, z, other.x, other.y, other.z);
    }

    @Override
    public int hashCode() {
        return hash == -1 ? hash = Objects.hash(x, y, z) : hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass() == WorldVectorSource.class) {
            WorldVectorSource other = (WorldVectorSource) obj;
            return Math.abs(this.x - other.x) < EPSILON && Math.abs(this.y - other.y) < EPSILON && Math.abs(this.z - other.z) < EPSILON;
        }

        return false;
    }

    @Override
    public String toString() {
        return "WorldVectorSource{x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
