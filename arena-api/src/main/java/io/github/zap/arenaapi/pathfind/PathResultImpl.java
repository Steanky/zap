package io.github.zap.arenaapi.pathfind;

import net.minecraft.server.v1_16_R3.PathEntity;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;

class PathResultImpl implements PathResult {
    private final LinkedHashSet<PathNode> nodes;
    private final PathNode source;
    private final PathNode destination;

    private final PathEntity pathEntity = null;

    PathResultImpl(@NotNull LinkedHashSet<PathNode> nodes, @NotNull PathNode source, @NotNull PathNode destination, boolean reachedTarget) {
        this.nodes = nodes;
        this.source = source;
        this.destination = destination;
    }

    @Override
    public @NotNull PathNode source() {
        return source;
    }

    @Override
    public @NotNull PathNode destination() {
        return destination;
    }

    @Override
    public @NotNull LinkedHashSet<PathNode> nodes() {
        return nodes;
    }

    @Override
    public @NotNull PathEntity toPathEntity() {
        return pathEntity;
    }
}
