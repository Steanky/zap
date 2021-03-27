package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public interface PathfinderContext {
    @NotNull List<PathfinderEngine.Entry> ongoingOperations();

    @NotNull Set<PathResult> successfulPaths();

    @NotNull Set<PathResult> failedPaths();

    @NotNull SnapshotProvider snapshotProvider();
}
