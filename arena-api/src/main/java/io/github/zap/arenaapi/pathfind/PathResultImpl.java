package io.github.zap.arenaapi.pathfind;

import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.PathEntity;
import net.minecraft.server.v1_16_R3.PathPoint;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class PathResultImpl implements PathResult {
    private final PathNode source;
    private final PathDestination destination;

    private final PathEntity pathEntity;

    PathResultImpl(@NotNull PathNode source, @NotNull PathDestination destination) {
        this.source = source;
        this.destination = destination;

        List<PathPoint> points = new ArrayList<>();
        do {
            points.add(new PathPoint(source.x, source.y, source.z));
            source = source.next;
        }
        while (source.next != null);

        PathNode node = destination.node();
        pathEntity = new PathEntity(points, new BlockPosition(node.x, node.y, node.z), true);
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
