package io.github.zap.arenaapi.vector;

import io.github.zap.arenaapi.util.VectorUtils;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WorldVector extends BlockVector {
    public static final double EPSILON = 1.0E-6D;

    private double x;
    private double y;
    private double z;
    private int hash;
    private boolean hashInvalid = true;

    public WorldVector(double x, double y, double z) {
        super((int)x, (int)y, (int)z);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public WorldVector(@NotNull Vector vector) {
        super(vector);
        x = vector.getX();
        y = vector.getY();
        z = vector.getZ();
    }

    public @NotNull WorldVector sum(@NotNull WorldVector other) {
        return new WorldVector(x + other.x, y + other.y, z + other.z);
    }

    public void sumSelf(@NotNull WorldVector other) {
        this.x += other.x;
        this.y += other.y;
        this.z += other.z;
        hashInvalid = true;
    }

    public void sumSelf(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        hashInvalid = true;
    }

    public @NotNull WorldVector product(@NotNull WorldVector other) {
        return new WorldVector(x * other.x, y * other.y, z * other.z);
    }

    public void productSelf(@NotNull WorldVector other) {
        this.x *= other.x;
        this.y *= other.y;
        this.z *= other.z;
        hashInvalid = true;
    }

    public void productSelf(double x, double y, double z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        hashInvalid = true;
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

    public double distanceSquared(@NotNull WorldVector other) {
        return VectorUtils.distanceSquared(x, y, z, other.x, other.y, other.z);
    }

    public double distance(@NotNull WorldVector other) {
        return VectorUtils.distance(x, y, z, other.x, other.y, other.z);
    }

    public WorldVector copy() {
        return new WorldVector(x, y, z);
    }

    @Override
    public int hashCode() {
        if(hashInvalid) {
            hashInvalid = false;
            return hash = Objects.hash(x, y, z);
        }

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass() == WorldVector.class) {
            WorldVector other = (WorldVector) obj;
            return Math.abs(this.x - other.x) < EPSILON && Math.abs(this.y - other.y) < EPSILON &&
                    Math.abs(this.z - other.z) < EPSILON;
        }

        return false;
    }

    @Override
    public String toString() {
        return "WorldVectorSource{x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
