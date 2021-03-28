package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface PathfinderContext {
    @NotNull List<PathfinderEngine.Entry> ongoingOperations();

    @NotNull List<PathResult> successfulPaths();

    @NotNull List<PathResult> failedPaths();

    @NotNull SnapshotProvider snapshotProvider();
}