package io.github.zap.arenaapi.pathfind;

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
        private final double width;
        private final double height;
        private final int hash;

        public Characteristics(double width, double height) {
            this.width = width;
            this.height = height;
            this.hash = Objects.hash(width, height);
        }

        public Characteristics() {
            this(0, 0);
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
            return "PathAgent.Characteristics{width=" + width + ", height=" + height + "}";
        }
    }

    /**
     * Returns an object representing the characteristics of this PathAgent that may be used to alter which nodes it
     * it is able to traverse.
     * @return A PathAgent.Characteristics object containing PathAgent data used to determine node navigability
     */
    Characteristics characteristics();

    /**
     * Constructs a new PathNode at the current block location of this agent.
     * @return A newly-constructed PathNode object located at the agent's current position
     */
    PathNode nodeAt();

    /**
     * Gets the position of this PathAgent.
     * @return The position of this PathAgent
     */
    Vector position();

    /**
     * Creates a new PathAgent from the given Entity. The resulting PathAgent will have the same width, height, and
     * vector position of the entity.
     * @param entity The entity from which to create a PathAgent
     * @return A PathAgent object corresponding to the given Entity
     */
    static PathAgent fromEntity(@NotNull Entity entity) {
        Objects.requireNonNull(entity, "entity cannot be null!");
        return new EntityAgent<>(entity);
    }

    static PathAgent fromVector(@NotNull Vector vector, @NotNull Characteristics characteristics) {
        Objects.requireNonNull(vector, "vector cannot be null!");
        Objects.requireNonNull(characteristics, "characteristics cannot be null!");

        return new VectorAgent(vector, characteristics);
    }

    static PathAgent fromVector(@NotNull Vector vector) {
        return fromVector(vector, new Characteristics());
    }
}
