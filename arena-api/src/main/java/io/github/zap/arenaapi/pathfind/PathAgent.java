package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.Vector3D;
import io.github.zap.vector.Vectors;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents an navigation-capable object. Commonly used to wrap Bukkit objects such as Entity. Provides information
 * which may be critical to determine the 'navigability' of a node using a Characteristics object.
 */
public interface PathAgent extends Vector3D {
    /**
     * Returns an object representing the characteristics of this PathAgent. It essentially defines what nodes the
     * agent may be able to traverse.
     * @return A PathAgent.Characteristics object containing PathAgent data used to determine node navigability
     */
    public abstract @NotNull AgentCharacteristics characteristics();

    /**
     * Creates a new PathAgent from the given Entity. The resulting PathAgent will have the same width, height, and
     * vector position of the entity.
     * @param entity The entity from which to create a PathAgent
     * @return A PathAgent object corresponding to the given Entity
     */
    public static @NotNull PathAgent fromEntity(@NotNull Entity entity) {
        Objects.requireNonNull(entity, "entity cannot be null!");
        Vector vector = entity.getLocation().toVector();
        return new PathAgentImpl(new AgentCharacteristics(entity), vector.getX(), vector.getY(), vector.getZ());
    }

    public static @NotNull PathAgent fromVector(@NotNull Vector vector, @NotNull AgentCharacteristics characteristics) {
        Objects.requireNonNull(vector, "vector cannot be null!");
        Objects.requireNonNull(characteristics, "characteristics cannot be null!");

        return new PathAgentImpl(characteristics, vector.getX(), vector.getY(), vector.getZ());
    }

    public static @NotNull PathAgent fromVector(@NotNull Vector vector) {
        return fromVector(vector, new AgentCharacteristics());
    }
}
