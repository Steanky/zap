package io.github.zap.arenaapi.pathfind.destination;

import io.github.zap.arenaapi.pathfind.path.PathTarget;
import io.github.zap.vector.Vector3I;
import org.jetbrains.annotations.NotNull;

public interface PathDestination extends Vector3I {
    @NotNull PathTarget target();
}
