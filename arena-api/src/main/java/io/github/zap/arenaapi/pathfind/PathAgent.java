package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.Vector3D;
import io.github.zap.vector.Vectors;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @NotNull AgentCharacteristics characteristics();

    /**
     * Creates a new PathAgent from the given Entity. The resulting PathAgent will have the same width, height, and
     * vector position of the entity.
     * @param entity The entity from which to create a PathAgent
     * @return A PathAgent object corresponding to the given Entity
     */
    static @Nullable PathAgent fromEntity(@NotNull Entity entity) {
        Vector3D location;
        Location entityLocation = entity.getLocation();
        if(!Utils.isValidLocation(entityLocation)) {
            return null;
        }

        if(!entity.isOnGround()) {
            double fallDistance = Utils.testFall(entityLocation);
            location = Vectors.of(entityLocation.getX(), entityLocation.getY() - fallDistance, entityLocation.getZ());
        }
        else {
            location = Vectors.of(entityLocation);
        }

        return new PathAgentImpl(new AgentCharacteristics(entity), location.x(), location.y(), location.z());
    }

    static @NotNull PathAgent fromVector(@NotNull Vector vector, @NotNull AgentCharacteristics characteristics) {
        return new PathAgentImpl(characteristics, vector.getX(), vector.getY(), vector.getZ());
    }

    static @NotNull PathAgent fromVector(@NotNull Vector vector) {
        return fromVector(vector, new AgentCharacteristics());
    }
}
