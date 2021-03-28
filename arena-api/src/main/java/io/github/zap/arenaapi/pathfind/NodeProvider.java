package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface NodeProvider {
    List<PathNode> nodesFrom(@NotNull PathfinderContext context, @NotNull PathNode node);
}
