package io.github.zap.arenaapi.pathfind.step;

import io.github.zap.arenaapi.pathfind.agent.PathAgent;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProvider;
import io.github.zap.arenaapi.pathfind.util.Direction;
import io.github.zap.vector.Vector3D;
import io.github.zap.vector.Vector3I;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface NodeStepper {
    @Nullable Vector3I stepDirectional(@NotNull BlockCollisionProvider collisionProvider,
                                       @NotNull PathAgent agent, @NotNull Vector3D position, @NotNull Direction direction);
}
