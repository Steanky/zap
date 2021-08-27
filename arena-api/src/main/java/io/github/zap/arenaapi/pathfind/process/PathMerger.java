package io.github.zap.arenaapi.pathfind.process;

import io.github.zap.arenaapi.pathfind.path.PathResult;
import io.github.zap.arenaapi.pathfind.context.PathfinderContext;
import io.github.zap.arenaapi.pathfind.operation.PathOperation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PathMerger {
    @Nullable PathResult attemptMerge(@NotNull PathOperation operation, @NotNull PathfinderContext context);
}
