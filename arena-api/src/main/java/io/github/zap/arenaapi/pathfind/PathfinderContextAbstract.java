package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;

abstract class PathfinderContextAbstract implements PathfinderContext {
    protected final Queue<PathResult> successfulPaths = new ArrayDeque<>();
    protected final Queue<PathResult> failedPaths = new ArrayDeque<>();

    protected final BlockCollisionProvider blockCollisionProvider;
    protected final PathMerger merger;
    protected final int pathCapacity;

    PathfinderContextAbstract(@NotNull BlockCollisionProvider blockCollisionProvider,
                              @NotNull PathMerger merger, int pathCapacity) {
        this.blockCollisionProvider = blockCollisionProvider;
        this.merger = merger;
        this.pathCapacity = pathCapacity;
    }

    @Override
    public void recordPath(@NotNull PathResult result) {}

    @Override
    public @NotNull BlockCollisionProvider blockProvider() {
        return blockCollisionProvider;
    }

    @Override
    public @NotNull PathMerger merger() {
        return merger;
    }

    @Override
    public @NotNull Collection<PathResult> failedPaths() {
        return failedPaths;
    }

    @Override
    public @NotNull Collection<PathResult> successfulPaths() {
        return successfulPaths;
    }
}