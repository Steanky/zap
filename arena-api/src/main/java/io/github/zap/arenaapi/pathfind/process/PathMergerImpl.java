package io.github.zap.arenaapi.pathfind.process;

import io.github.zap.arenaapi.pathfind.path.PathResult;
import io.github.zap.arenaapi.pathfind.context.PathfinderContext;
import io.github.zap.arenaapi.pathfind.operation.PathOperation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class PathMergerImpl implements PathMerger {
    @Override
    public @Nullable PathResult attemptMerge(@NotNull PathOperation operation, @NotNull PathfinderContext context) {
        //TODO: implement good path merging
        return null;
    }
}
