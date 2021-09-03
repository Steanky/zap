package io.github.zap.vector;

import com.google.common.math.DoubleMath;
import org.bukkit.Location;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class Vectors {
    public static final double EPSILON = 0.000001;
    public static final Vector3I ZERO = new Vector3IImpl(0,0, 0);

    private record Vector3DImpl(double x, double y, double z) implements Vector3D {
        @Override
        public String toString() {
            return "Vector3D{x=" + x + ", y=" + y + ", z=" + z + "}";
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Vector3I vector) {
                return vector.x() == x && vector.y() == y && vector.z() == z;
            }

            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
    }

    private record Vector3IImpl(int x, int y, int z) implements Vector3I {
        @Override
        public String toString() {
            return "Vector3I{x=" + x + ", y=" + y + ", z=" + z + "}";
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Vector3I vector) {
                return vector.x() == x && vector.y() == y && vector.z() == z;
            }

            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
    }

    private record Vector2IImpl(int x, int z) implements Vector2I {
        @Override
        public String toString() {
            return "Vector2I{x=" + x + ", z=" + z + "}";
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Vector2I vector) {
                return vector.x() == x && vector.z() == z;
            }

            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }
    }

    public static @NotNull Vector3D of(@NotNull Vector vector) {
        return new Vector3DImpl(vector.getX(), vector.getY(), vector.getZ());
    }

    public static @NotNull Vector3D of(@NotNull Location location) {
        Vector vector = location.toVector();
        return new Vector3DImpl(vector.getX(), vector.getY(), vector.getZ());
    }

    public static @NotNull Vector3D of(double x, double y, double z) {
        return new Vector3DImpl(x, y, z);
    }

    public static @NotNull Vector3I of(int x, int y, int z) {
        return new Vector3IImpl(x, y, z);
    }

    public static @NotNull Vector2I of(int x, int z) {
        return new Vector2IImpl(x, z);
    }

    public static Vector3I blockVector(double x, double y, double z) {
        return new Vector3IImpl(NumberConversions.floor(x), NumberConversions.floor(y), NumberConversions.floor(z));
    }

    public static @NotNull Vector3D copy(@NotNull Vector3D other) {
        return new Vector3DImpl(other.x(), other.y(), other.z());
    }

    public static @NotNull Vector3I copy(@NotNull Vector3I other) {
        return new Vector3IImpl(other.x(), other.y(), other.z());
    }

    public static @NotNull Vector asBukkit(@NotNull Vector3I vector) {
        return new Vector(vector.x(), vector.y(), vector.z());
    }

    public static @NotNull Vector asBukkit(@NotNull Vector3D vector) {
        return new Vector(vector.x(), vector.y(), vector.z());
    }

    public static @NotNull Vector3D asDouble(@NotNull Vector3I other) {
        return new Vector3DImpl(other.x(), other.y(), other.z());
    }

    public static @NotNull Vector3I asIntTruncate(@NotNull Vector3D other) {
        return new Vector3IImpl((int)other.x(), (int)other.y(), (int)other.z());
    }

    public static @NotNull Vector3I asIntFloor(@NotNull Vector3D other) {
        return new Vector3IImpl(NumberConversions.floor(other.x()), NumberConversions.floor(other.y()),
                NumberConversions.floor(other.z()));
    }

    public static @NotNull Vector3I asIntFloor(double x, double y, double z) {
        return new Vector3IImpl(NumberConversions.floor(x), NumberConversions.floor(y), NumberConversions.floor(z));
    }

    public static @NotNull Vector3I asChunkRelative(@NotNull Vector3I worldRelative) {
        return new Vector3IImpl(worldRelative.x() & 15, worldRelative.y(), worldRelative.z() & 15);
    }

    public static @NotNull Vector2I asChunk(@NotNull Vector3I worldRelative) {
        return new Vector2IImpl(worldRelative.x() >> 4, worldRelative.z() >> 4);
    }

    public static @NotNull Vector2I asChunk(@NotNull Vector3D worldRelative) {
        return new Vector2IImpl(((int)Math.floor(worldRelative.x())) >> 4, ((int)Math.floor(worldRelative.z())) >> 4);
    }

    public static boolean isFinite(@NotNull Vector3D vector) {
        return Double.isFinite(vector.x()) && Double.isFinite(vector.y()) && Double.isFinite(vector.z());
    }

    public static @NotNull Vector3I asWorldRelative(@NotNull Vector3I chunkRelative, @NotNull Vector2I chunk) {
        if(validChunkRelative(chunkRelative)) {
            return new Vector3IImpl((chunk.x() << 4) + chunkRelative.x(),
                    chunkRelative.y(), (chunk.z() << 4) + chunkRelative.z());
        }

        throw new IllegalArgumentException("chunkRelative coordinates invalid: " + chunkRelative);
    }

    public static double distance(@NotNull Vector3D first, @NotNull Vector3D second) {
        return Math.sqrt(distanceSquared(first, second));
    }

    public static double distance(@NotNull Vector3D first, @NotNull Vector3I second) {
        return Math.sqrt(distanceSquared(first, second));
    }

    public static double distance(@NotNull Vector3I first, @NotNull Vector3D second) {
        return Math.sqrt(distanceSquared(first, second));
    }

    public static double distance(@NotNull Vector3I first, @NotNull Vector3I second) {
        return Math.sqrt(distanceSquared(first, second));
    }

    public static double distanceSquared(@NotNull Vector3D first, @NotNull Vector3D second) {
        return distanceSquared(first.x(), first.y(), first.z(), second.x(), second.y(), second.z());
    }

    public static double distanceSquared(@NotNull Vector3D first, @NotNull Vector3I second) {
        return distanceSquared(first.x(), first.y(), first.z(), second.x(), second.y(), second.z());
    }

    public static double distanceSquared(@NotNull Vector3I first, @NotNull Vector3D second) {
        return distanceSquared(first.x(), first.y(), first.z(), second.x(), second.y(), second.z());
    }

    public static int distanceSquared(@NotNull Vector3I first, @NotNull Vector3I second) {
        return distanceSquared(first.x(), first.y(), first.z(), second.x(), second.y(), second.z());
    }

    public static int distanceSquared(int x1, int y1, int z1, int x2, int y2, int z2) {
        int xDif = x2 - x1;
        int yDif = y2 - y1;
        int zDif = z2 - z1;

        return (xDif * xDif) + (yDif * yDif) + (zDif * zDif);
    }

    public static double distanceSquared(double x1, double y1, double z1, double x2, double y2, double z2) {
        double xDif = x2 - x1;
        double yDif = y2 - y1;
        double zDif = z2 - z1;

        return (xDif * xDif) + (yDif * yDif) + (zDif * zDif);
    }

    public static double magnitude(double x, double y, double z) {
        return Math.sqrt((x * x) + (y * y) + (z * z));
    }

    public static double magnitudeSquared(double x, double y, double z) {
        return (x * x) + (y * y) + (z * z);
    }

    public static double magnitude(@NotNull Vector3D vector) {
        return magnitude(vector.x(), vector.y(), vector.z());
    }

    public static double magnitudeSquared(@NotNull Vector3D vector) {
        return magnitudeSquared(vector.x(), vector.y(), vector.z());
    }

    public static @NotNull Vector3I abs(@NotNull Vector3I vector) {
        return new Vector3IImpl(Math.abs(vector.x()), Math.abs(vector.y()), Math.abs(vector.z()));
    }

    public static @NotNull Vector3D abs(@NotNull Vector3D vector) {
        return new Vector3DImpl(Math.abs(vector.x()), Math.abs(vector.y()), Math.abs(vector.z()));
    }

    public static int dotProduct(@NotNull Vector3I first, @NotNull Vector3I second) {
        return (first.x() * second.x()) + (first.y() * second.y()) + (first.z() * second.z());
    }

    public static double dotProduct(@NotNull Vector3D first, @NotNull Vector3I second) {
        return (first.x() * second.x()) + (first.y() * second.y()) + (first.z() * second.z());
    }

    public static double dotProduct(@NotNull Vector3I first, @NotNull Vector3D second) {
        return (first.x() * second.x()) + (first.y() * second.y()) + (first.z() * second.z());
    }

    public static double dotProduct(@NotNull Vector3D first, @NotNull Vector3D second) {
        return (first.x() * second.x()) + (first.y() * second.y()) + (first.z() * second.z());
    }

    public static @NotNull Vector3D add(@NotNull Vector3D first, @NotNull Vector3D second) {
        return new Vector3DImpl(first.x() + second.x(), first.y() + second.y(), first.z() + second.z());
    }

    public static @NotNull Vector3D add(@NotNull Vector3D first, @NotNull Vector3I second) {
        return new Vector3DImpl(first.x() + second.x(), first.y() + second.y(), first.z() + second.z());
    }

    public static @NotNull Vector3D add(@NotNull Vector3I first, @NotNull Vector3D second) {
        return new Vector3DImpl(first.x() + second.x(), first.y() + second.y(), first.z() + second.z());
    }

    public static @NotNull Vector3I add(@NotNull Vector3I first, @NotNull Vector3I second) {
        return new Vector3IImpl(first.x() + second.x(), first.y() + second.y(), first.z() + second.z());
    }

    public static @NotNull Vector3I add(@NotNull Vector3I first, int second) {
        return new Vector3IImpl(first.x() + second, first.y() + second, first.z() + second);
    }

    public static @NotNull Vector3D add(@NotNull Vector3D first, double second) {
        return new Vector3DImpl(first.x() + second, first.y() + second, first.z() + second);
    }

    public static @NotNull Vector3D subtract(@NotNull Vector3D first, @NotNull Vector3D second) {
        return new Vector3DImpl(first.x() - second.x(), first.y() - second.y(), first.z() - second.z());
    }

    public static @NotNull Vector3D subtract(@NotNull Vector3D first, @NotNull Vector3I second) {
        return new Vector3DImpl(first.x() - second.x(), first.y() - second.y(), first.z() - second.z());
    }

    public static @NotNull Vector3D subtract(@NotNull Vector3I first, @NotNull Vector3D second) {
        return new Vector3DImpl(first.x() - second.x(), first.y() - second.y(), first.z() - second.z());
    }

    public static @NotNull Vector3I subtract(@NotNull Vector3I first, @NotNull Vector3I second) {
        return new Vector3IImpl(first.x() - second.x(), first.y() - second.y(), first.z() - second.z());
    }

    public static @NotNull Vector3I subtract(@NotNull Vector3I first, int second) {
        return new Vector3IImpl(first.x() - second, first.y() - second, first.z() - second);
    }

    public static @NotNull Vector3D subtract(@NotNull Vector3I first, double second) {
        return new Vector3DImpl(first.x() - second, first.y() - second, first.z() - second);
    }

    public static @NotNull Vector3D multiply(@NotNull Vector3D first, @NotNull Vector3D second) {
        return new Vector3DImpl(first.x() * second.x(), first.y() * second.y(), first.z() * second.z());
    }

    public static @NotNull Vector3D multiply(@NotNull Vector3D first, @NotNull Vector3I second) {
        return new Vector3DImpl(first.x() * second.x(), first.y() * second.y(), first.z() * second.z());
    }

    public static @NotNull Vector3D multiply(@NotNull Vector3I first, @NotNull Vector3D second) {
        return new Vector3DImpl(first.x() * second.x(), first.y() * second.y(), first.z() * second.z());
    }

    public static @NotNull Vector3I multiply(@NotNull Vector3I first, @NotNull Vector3I second) {
        return new Vector3IImpl(first.x() * second.x(), first.y() * second.y(), first.z() * second.z());
    }

    public static @NotNull Vector3I multiply(@NotNull Vector3I first, int second) {
        return new Vector3IImpl(first.x() * second, first.y() * second, first.z() * second);
    }

    public static @NotNull Vector3D multiply(@NotNull Vector3I first, double second) {
        return new Vector3DImpl(first.x() * second, first.y() * second, first.z() * second);
    }

    public static @NotNull Vector3D multiply(@NotNull Vector3D first, double second) {
        return new Vector3DImpl(first.x() * second, first.y() * second, first.z() * second);
    }

    public static @NotNull Vector3D multiply(@NotNull Vector3D first, int second) {
        return new Vector3DImpl(first.x() * second, first.y() * second, first.z() * second);
    }

    public static @NotNull Vector3D divide(@NotNull Vector3D first, @NotNull Vector3D second) {
        return new Vector3DImpl(first.x() / second.x(), first.y() / second.y(), first.z() / second.z());
    }

    public static @NotNull Vector3D divide(@NotNull Vector3D first, @NotNull Vector3I second) {
        return new Vector3DImpl(first.x() / second.x(), first.y() / second.y(), first.z() / second.z());
    }

    public static @NotNull Vector3D divide(@NotNull Vector3I first, @NotNull Vector3D second) {
        return new Vector3DImpl(first.x() / second.x(), first.y() / second.y(), first.z() / second.z());
    }

    public static @NotNull Vector3I divide(@NotNull Vector3I first, @NotNull Vector3I second) {
        return new Vector3IImpl(first.x() / second.x(), first.y() / second.y(), first.z() / second.z());
    }

    public static @NotNull Vector3I divide(@NotNull Vector3I first, int second) {
        return new Vector3IImpl(first.x() / second, first.y() / second, first.z() / second);
    }

    public static @NotNull Vector3D divide(@NotNull Vector3D first, double second) {
        return new Vector3DImpl(first.x() / second, first.y() / second, first.z() / second);
    }

    public static boolean validChunkRelative(@NotNull Vector3I vector) {
        return vector.x() >= 0 && vector.y() >= 0 && vector.z() >= 0 &&
                vector.x() < 16 && vector.y() < 256 && vector.z() < 16;
    }

    public static boolean equals(@NotNull Vector3D first, @NotNull Vector3D second) {
        return first.x() == second.x() && first.y() == second.y() && first.z() == second.z();
    }

    public static boolean fuzzyEquals(@NotNull Vector3D first, @NotNull Vector3D second) {
        return DoubleMath.fuzzyEquals(first.x(), second.x(), EPSILON) &&
                DoubleMath.fuzzyEquals(first.y(), second.y(), EPSILON) &&
                DoubleMath.fuzzyEquals(first.z(), second.z(), EPSILON);
    }

    public static boolean fuzzyEquals(@NotNull Vector3D first, @NotNull Vector3I second) {
        return DoubleMath.fuzzyEquals(first.x(), second.x(), EPSILON) &&
                DoubleMath.fuzzyEquals(first.y(), second.y(), EPSILON) &&
                DoubleMath.fuzzyEquals(first.z(), second.z(), EPSILON);
    }

    public static boolean fuzzyEquals(@NotNull Vector3I first, @NotNull Vector3D second) {
        return DoubleMath.fuzzyEquals(first.x(), second.x(), EPSILON) &&
                DoubleMath.fuzzyEquals(first.y(), second.y(), EPSILON) &&
                DoubleMath.fuzzyEquals(first.z(), second.z(), EPSILON);
    }

    public static boolean equals(@NotNull Vector3I first, @NotNull Vector3I second) {
        return first.x() == second.x() && first.y() == second.y() && first.z() == second.z();
    }

    public static boolean equals(@NotNull Vector3I first, int x, int y, int z) {
        return first.x() == x && first.y() == y && first.z() == z;
    }
}