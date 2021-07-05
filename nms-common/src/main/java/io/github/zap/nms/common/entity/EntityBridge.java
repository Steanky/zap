package io.github.zap.nms.common.entity;

import io.github.zap.nms.common.pathfind.MobNavigator;
import io.github.zap.nms.common.pathfind.PathEntityWrapper;
import io.github.zap.nms.common.pathfind.PathPointWrapper;
import io.github.zap.vector.VectorAccess;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import java.util.List;
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
                                              @NotNull VectorAccess destination, boolean reachesDestination);

    @NotNull PathPointWrapper makePathPoint(@NotNull VectorAccess blockLocation);

    @NotNull MobNavigator overrideNavigatorFor(Mob mob) throws NoSuchFieldException, IllegalAccessException;
}
