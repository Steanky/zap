package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface PathfinderContext {
    @NotNull BlockCollisionProvider blockProvider();

    void recordPath(@NotNull PathResult result);

    @NotNull PathMerger merger();

    @NotNull Collection<PathResult> failedPaths();

    @NotNull Collection<PathResult> successfulPaths();
}
