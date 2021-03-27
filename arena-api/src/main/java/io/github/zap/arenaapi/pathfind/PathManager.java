package io.github.zap.arenaapi.pathfind;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PathManager {
    private final WorldSnapshotProvider snapshotProvider;
    private final List<PathOperation> operations = new ArrayList<>();

    public PathManager(@NotNull World world) {
        snapshotProvider = new WorldSnapshotProvider(world);
    }
}
