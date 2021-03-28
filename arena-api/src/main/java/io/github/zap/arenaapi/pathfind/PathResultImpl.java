package io.github.zap.arenaapi.pathfind;

import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.PathEntity;
import net.minecraft.server.v1_16_R3.PathPoint;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

class PathResultImpl implements PathResult {
    private final LinkedHashSet<PathNode> nodes;
    private final PathNode source;
    private final PathNode destination;

    private final PathEntity pathEntity;

    PathResultImpl(@NotNull LinkedHashSet<PathNode> nodes, @NotNull PathNode source, @NotNull PathNode destination, boolean reachedTarget) {
        this.nodes = nodes;
        this.source = source;
        this.destination = destination;

        pathEntity = new PathEntity(toNms(nodes), new BlockPosition(destination.x, destination.y, destination.z), reachedTarget);
    }

    private List<PathPoint> toNms(Collection<PathNode> nodes) {
        List<PathPoint> points = new ArrayList<>();
        for(PathNode node : nodes) {
            points.add(node.toNms());
        }

        return points;
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
