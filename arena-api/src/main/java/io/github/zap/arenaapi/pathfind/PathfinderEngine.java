package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.Disposable;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

public interface PathfinderEngine extends Disposable {
    @NotNull Future<PathResult> queueOperation(@NotNull PathOperation operation);

    boolean isAsync();
}
