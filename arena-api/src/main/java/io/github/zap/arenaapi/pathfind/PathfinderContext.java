package io.github.zap.arenaapi.pathfind;

import java.util.List;

public interface PathfinderContext {
    List<PathfinderEngine.Entry> ongoingOperations();

    SnapshotProvider snapshotProvider();
}
