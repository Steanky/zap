package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.graph.ChunkGraph;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.Semaphore;

abstract class PathfinderContextAbstract implements PathfinderContext {
    private final Queue<PathResult> successfulPaths = new ArrayDeque<>();

    private final BlockCollisionProvider blockCollisionProvider;
    private final PathMerger merger;
    private final int pathCapacity;

    PathfinderContextAbstract(@NotNull BlockCollisionProvider blockCollisionProvider,
                              @NotNull PathMerger merger, int pathCapacity) {
        this.blockCollisionProvider = blockCollisionProvider;
        this.merger = merger;
        this.pathCapacity = pathCapacity;
    }

    @Override
    public @NotNull BlockCollisionProvider blockProvider() {
        return blockCollisionProvider;
    }

    @Override
    public void recordPath(@NotNull PathResult path) {
        PathOperation.State state = path.state();

        if (state == PathOperation.State.SUCCEEDED) {
            handleAddition(path, successfulPaths);
        }
    }

    @Override
    public @NotNull PathMerger merger() {
        return merger;
    }

    @Override
    public @NotNull Collection<PathResult> failedPaths() {
        return new ArrayList<>();
    }

    @Override
    public @NotNull Collection<PathResult> successfulPaths() {
        return successfulPaths;
    }

    private void handleAddition(PathResult result, Queue<PathResult> target) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (target) {
            int oldCount = target.size();
            int newCount = oldCount + 1;

            target.add(result);

            if(newCount == pathCapacity) {
                target.poll();
            }
        }
    }
}