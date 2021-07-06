package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class PathHandler {
    private final PathfinderEngine engine;
    private Future<PathResult> result;

    public PathHandler(@NotNull PathfinderEngine engine) {
        this.engine = engine;
    }

    public void queueOperation(@NotNull PathOperation operation, @NotNull World world) {
        if(result == null || result.isDone() || result.isCancelled()) {
            result = engine.giveOperation(operation, world);
        }
    }

    public @Nullable PathResult tryTakeResult() {
        if(result.isDone() && !result.isCancelled()) {
            try {
                return result.get();
            } catch (InterruptedException | ExecutionException exception) {
                ArenaApi.warning("Exception thrown when retrieving a completed PathResult:");
                exception.printStackTrace();
            }
        }

        return null;
    }
}
