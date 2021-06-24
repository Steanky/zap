package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.ImmutableWorldVector;
import io.github.zap.vector.Positional;
import io.github.zap.vector.VectorAccess;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents an navigation-capable object. Commonly used to wrap Bukkit objects such as Entity. Provides information
 * which may be critical to determine the 'navigability' of a node using a Characteristics object.
 */
public abstract class PathAgent implements Positional {
    public static class Characteristics {
        private static final double DEFAULT_JUMP_HEIGHT = 1.125D;

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
            this(width, height, DEFAULT_JUMP_HEIGHT);
        }

        public Characteristics(@NotNull Entity fromEntity) {
            this(fromEntity.getWidth(), fromEntity.getHeight(), DEFAULT_JUMP_HEIGHT);
        }

        public Characteristics() {
            this(0, 0, DEFAULT_JUMP_HEIGHT);
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

    private final ImmutableWorldVector position;

    PathAgent(@NotNull ImmutableWorldVector position) {
        this.position = position;
    }

    @Override
    public @NotNull VectorAccess position() {
        return position;
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
        return new PathAgentImpl(new Characteristics(entity), VectorAccess.immutable(entity.getLocation().toVector()));
    }

    public static @NotNull PathAgent fromVector(@NotNull Vector vector, @NotNull Characteristics characteristics) {
        Objects.requireNonNull(vector, "vector cannot be null!");
        Objects.requireNonNull(characteristics, "characteristics cannot be null!");

        return new PathAgentImpl(characteristics, VectorAccess.immutable(vector));
    }

    public static @NotNull PathAgent fromVector(@NotNull Vector vector) {
        return fromVector(vector, new Characteristics());
    }
}
