package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface PathMerger {
    @Nullable PathResult attemptMerge(@NotNull PathOperation operation, @NotNull Collection<PathResult> results);
}
