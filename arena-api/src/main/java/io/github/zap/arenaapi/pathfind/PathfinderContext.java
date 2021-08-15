package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.Semaphore;

public interface PathfinderContext {
    @NotNull BlockCollisionProvider blockProvider();

    void recordPath(@NotNull PathResult result);

    @NotNull PathMerger merger();

    @NotNull Collection<PathResult> failedPaths();

    @NotNull Collection<PathResult> successfulPaths();
}
