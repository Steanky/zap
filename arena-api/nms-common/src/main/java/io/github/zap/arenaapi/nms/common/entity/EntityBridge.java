package io.github.zap.arenaapi.nms.common.entity;

import io.github.zap.arenaapi.nms.common.pathfind.MobNavigator;
import io.github.zap.arenaapi.nms.common.pathfind.PathEntityWrapper;
import io.github.zap.arenaapi.nms.common.pathfind.PathPointWrapper;
import io.github.zap.vector.Vector3I;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Piglin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * A bridge for static methods from or directly relating to NMS Entity.
 */
public interface EntityBridge {
    /**
     * Returns the next entity ID, using the same generator function as NMS Entity.
     * @return A new entity ID
     */
    int nextEntityID();

    /**
     * Returns a random UUID. The UUID is created using the same random generator as NMS Entity.
     * @return A new random UUID
     */
    @NotNull UUID randomUUID();

    /**
     * Returns the NMS type ID for the given Bukkit EntityType. If the EntityType is not valid, it will return -1.
     * @param type The EntityType to retrieve the ID for
     * @return The NMS ID for the EntityType
     */
    int getEntityTypeID(@NotNull EntityType type);

    @NotNull PathEntityWrapper makePathEntity(@NotNull List<PathPointWrapper> pointWrappers,
                                              @NotNull Vector3I destination, boolean reachesDestination);

    @NotNull PathPointWrapper makePathPoint(@NotNull Vector3I blockLocation);

    @NotNull MobNavigator overrideNavigatorFor(Mob mob) throws NoSuchFieldException, IllegalAccessException;

    /**
     * Calculates the entity's distance to a location based on its NMS calculation
     * @param entity The entity to check distance from
     * @param x The x-coordinate of the test location
     * @param y The y-coordinate of the test location
     * @param z The z-coordinate of the test location
     * @return The distance
     */
    double distanceTo(@NotNull Entity entity, double x, double y, double z);

    /**
     * Gets the {@link Random} used by a {@link LivingEntity}
     * @param livingEntity The living entity to get the random from
     * @return The random
     */
    @NotNull Random getRandomFor(@NotNull LivingEntity livingEntity);

    /**
     * Checks whether a {@link Mob} can see a potential {@link Entity target}
     * @param mob The mob to check
     * @param target The target to test
     * @return Whether the mob can see the target
     */
    boolean canSee(@NotNull Mob mob, @NotNull Entity target);

    /**
     * Updates the direction a {@link Mob} is looking at
     * @param mob The mob to update
     * @param target The target of the mob to look at
     * @param maxYawChange The maximum change in yaw
     * @param maxPitchChange The maximum change in pitch
     */
    void setLookDirection(@NotNull Mob mob, @NotNull Entity target, float maxYawChange, float maxPitchChange);

    /**
     * Determines whether a {@link LivingEntity} has a specific {@link Attribute}
     * @param livingEntity The living entity to test
     * @param attribute The attribute to look for
     * @return Whether the living entity has the attribute
     */
    boolean hasAttribute(@NotNull LivingEntity livingEntity, @NotNull Attribute attribute);

    /**
     * Sets an attribute for a {@link LivingEntity}
     * @param livingEntity The entity to set the attribute for
     * @param attribute The attribute to set
     * @param value Its new value
     */
    void setAttributeFor(@NotNull LivingEntity livingEntity, @NotNull Attribute attribute, double value);

}
