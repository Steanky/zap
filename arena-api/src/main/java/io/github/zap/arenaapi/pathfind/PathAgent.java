package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector.WorldVector;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents an navigation-capable object. Commonly used to wrap Bukkit objects such as Entity. Provides information
 * which may be critical to determine the 'navigability' of a node using a Characteristics object.
 */
public abstract class PathAgent extends WorldVector {
    public static class Characteristics {
        public final double width;
        public final double height;
        public final double jumpHeight;
        private final int hash;

        public Characteristics(double width, double height, double jumpHeight) {
            this.width = width;
            this.height = height;
            this.jumpHeight = jumpHeight;
            this.hash = Objects.hash(width, height, jumpHeight);
        }

        public Characteristics(double width, double height) {
            this(width, height, 1);
        }

        public Characteristics(@NotNull Entity fromEntity) {
            this(fromEntity.getWidth(), fromEntity.getHeight(), 1);
        }

        public Characteristics() {
            this(0, 0, 1);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Characteristics) {
                Characteristics other = (Characteristics) obj;
                return width == other.width && height == other.height && jumpHeight == other.jumpHeight;
            }

            return false;
        }

        @Override
        public String toString() {
            return "PathAgent.Characteristics{width=" + width + ", height=" + height + "jumpHeight=" + jumpHeight + "}";
        }
    }

    private final double x;
    private final double y;
    private final double z;

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
        return z;
    }

    protected PathAgent(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Returns an object representing the characteristics of this PathAgent. It essentially defines what nodes the
     * agent may be able to traverse.
     * @return A PathAgent.Characteristics object containing PathAgent data used to determine node navigability
     */
    public abstract @NotNull Characteristics characteristics();

    /**
     * Creates a new PathAgent from the given Entity. The resulting PathAgent will have the same width, height, and
     * vector position of the entity.
     * @param entity The entity from which to create a PathAgent
     * @return A PathAgent object corresponding to the given Entity
     */
    public static @NotNull PathAgent fromEntity(@NotNull Entity entity) {
        Objects.requireNonNull(entity, "entity cannot be null!");
        return new PathAgentImpl(new Characteristics(entity), WorldVector.immutable(entity.getLocation().toVector()));
    }

    public static @NotNull PathAgent fromVector(@NotNull Vector vector, @NotNull Characteristics characteristics) {
        Objects.requireNonNull(vector, "vector cannot be null!");
        Objects.requireNonNull(characteristics, "characteristics cannot be null!");

        return new PathAgentImpl(characteristics, WorldVector.immutable(vector));
    }

    public static @NotNull PathAgent fromVector(@NotNull Vector vector) {
        return fromVector(vector, new Characteristics());
    }
}
