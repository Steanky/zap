package io.github.zap.arenaapi.pathfind;

import net.minecraft.server.v1_16_R3.PathEntity;
import org.jetbrains.annotations.NotNull;

class PathResultImpl implements PathResult {
    private final PathNode source;
    private final PathDestination destination;

    private final PathEntity pathEntity = null;

    PathResultImpl(@NotNull PathNode source, @NotNull PathDestination destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    public @NotNull PathNode source() {
        return source;
    }

    @Override
    public @NotNull PathDestination destination() {
        return destination;
    }

    @Override
    public @NotNull PathEntity toPathEntity() {
        return pathEntity;
    }
}
