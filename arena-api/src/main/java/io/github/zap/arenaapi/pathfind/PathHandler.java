package io.github.zap.arenaapi.pathfind;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class PathHandler {
    public record Entry(PathOperation operation, PathResult result) {}

    private final PathfinderEngine engine;
    private final AtomicReference<Entry> result = new AtomicReference<>();
    private final AtomicBoolean working = new AtomicBoolean();
    private final AtomicBoolean complete = new AtomicBoolean();

    public PathHandler(@NotNull PathfinderEngine engine) {
        this.engine = engine;
    }

    public void queueOperation(@NotNull PathOperation operation, @NotNull World world) {
        if(working.compareAndSet(false, true)) {
            engine.giveOperation(operation, world, (result) -> {
                this.result.set(new Entry(operation, result));
                complete.set(true);
                working.set(false);
            });
        }
    }

    public @Nullable Entry takeResult() {
        if(complete.compareAndSet(true, false)) {
            return result.getAndSet(null);
        }

        return null;
    }

    public boolean isComplete() {
        return complete.get();
    }

    public boolean isWorking() {
        return working.get();
    }
}
