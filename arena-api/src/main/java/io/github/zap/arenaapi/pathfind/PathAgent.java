package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector.WorldVectorSource;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents an navigation-capable object. Commonly used to wrap Bukkit objects such as Entity. Provides information
 * which may be critical to determine the 'navigability' of a node using a Characteristics object.
 */
public interface PathAgent {
    class Characteristics {
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
                return width == other.width && height == other.height;
            }

            return false;
        }

        @Override
        public String toString() {
            return "PathAgent.Characteristics{width=" + width + ", height=" + height + "jumpHeight=" + jumpHeight + "}";
        }
    }

    /**
     * Returns an object representing the characteristics of this PathAgent that may be used to alter which nodes it
     * it is able to traverse.
     * @return A PathAgent.Characteristics object containing PathAgent data used to determine node navigability
     */
    @NotNull Characteristics characteristics();

    /**
     * Gets the position of this PathAgent.
     * @return The position of this PathAgent
     */
    @NotNull WorldVectorSource position();

    /**
     * Creates a new PathAgent from the given Entity. The resulting PathAgent will have the same width, height, and
     * vector position of the entity.
     * @param entity The entity from which to create a PathAgent
     * @return A PathAgent object corresponding to the given Entity
     */
    static @NotNull PathAgent fromEntity(@NotNull Entity entity) {
        Objects.requireNonNull(entity, "entity cannot be null!");
        return new PathAgentImpl(new Characteristics(entity), new WorldVectorSource(entity.getLocation().toVector()));
    }

    static @NotNull PathAgent fromVector(@NotNull Vector vector, @NotNull Characteristics characteristics) {
        Objects.requireNonNull(vector, "vector cannot be null!");
        Objects.requireNonNull(characteristics, "characteristics cannot be null!");

        return new PathAgentImpl(characteristics, new WorldVectorSource(vector));
    }

    static @NotNull PathAgent fromVector(@NotNull Vector vector) {
        return fromVector(vector, new Characteristics());
    }
}
