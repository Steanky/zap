package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.Vector3I;
import org.jetbrains.annotations.NotNull;

class PathDestinationImpl implements PathDestination {
    private final PathTarget target;
    private final int x;
    private final int y;
    private final int z;

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

    @Override
    public int x() {
        return x;
    }

    @Override
    public int y() {
        return y;
    }

    @Override
    public int z() {
        return z;
    }
}
