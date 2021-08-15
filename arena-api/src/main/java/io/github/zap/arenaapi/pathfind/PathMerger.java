package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PathMerger {
    @Nullable PathResult attemptMerge(@NotNull PathOperation operation, @NotNull PathfinderContext context);
}
