package io.github.zap.arenaapi.pathfind;

import net.minecraft.server.v1_16_R3.PathEntity;

import java.util.LinkedHashSet;

public interface PathResult {
    PathNode source();

    PathNode destination();

    LinkedHashSet<PathNode> nodes();

    PathEntity toPathEntity();
}
