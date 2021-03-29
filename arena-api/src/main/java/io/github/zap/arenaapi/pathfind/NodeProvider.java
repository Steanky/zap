package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

public interface NodeProvider {
    PathNode[] generateNodes(@NotNull PathfinderContext context, @NotNull PathOperation operation, @NotNull PathNode node);
}
