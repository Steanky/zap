package io.github.zap.arenaapi.pathfind;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents an navigation-capable object. Commonly used to wrap basic Bukkit objects such as Entity. Provides
 * information which may be critical to determine the 'navigability' of a node, such as agent width and height.
 */
public interface PathAgent {
    /**
     * Get the width of this Agent, which will be > 0 and finite.
     * @return The positive, finite width of this Agent
     */
    double getWidth();

    /**
     * Get the height of this Agent, which will be > 0 and finite.
     * @return The positive, finite height of this Agent
     */
    double getHeight();

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

    static PathAgent fromVector(@NotNull Vector vector, double width, double height) {
        Objects.requireNonNull(vector, "vector cannot be null!");
        Validate.isTrue(width >= 0, "width cannot be less than 0");
        Validate.isTrue(height >= 0, "height cannot be less than 0");
        Validate.isTrue(Double.isFinite(width), "width must be finite");
        Validate.isTrue(Double.isFinite(height), "height must be finite");

        return new VectorAgent(vector, width, height);
    }

    static PathAgent fromVector(@NotNull Vector vector) {
        return fromVector(vector, 0, 0);
    }
}
