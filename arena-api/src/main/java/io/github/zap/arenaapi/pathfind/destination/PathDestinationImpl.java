package io.github.zap.arenaapi.pathfind.destination;

import io.github.zap.arenaapi.pathfind.path.PathTarget;
import org.jetbrains.annotations.NotNull;

record PathDestinationImpl(PathTarget target, int x, int y, int z) implements PathDestination {
    PathDestinationImpl(@NotNull PathTarget target, int x, int y, int z) {
        this.target = target;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public @NotNull PathTarget target() {
        return target;
    }
}
