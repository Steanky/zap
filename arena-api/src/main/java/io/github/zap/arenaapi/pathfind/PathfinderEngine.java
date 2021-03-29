package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.Disposable;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface PathfinderEngine extends Disposable {
    void queueOperation(@NotNull PathOperation operation, @NotNull World world, @NotNull Consumer<PathResult> resultConsumer);

    boolean isAsync();

    static PathfinderEngine async() {
        return new AsyncPathfinderEngine();
    }
}
