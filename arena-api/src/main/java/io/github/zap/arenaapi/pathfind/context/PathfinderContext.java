package io.github.zap.arenaapi.pathfind.context;

import io.github.zap.arenaapi.pathfind.process.PathMerger;
import io.github.zap.arenaapi.pathfind.path.PathResult;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface PathfinderContext {
    @NotNull BlockCollisionProvider blockProvider();

    void recordPath(@NotNull PathResult result);

    @NotNull PathMerger merger();

    @NotNull Collection<PathResult> failedPaths();

    @NotNull Collection<PathResult> successfulPaths();
}
