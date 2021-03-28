package io.github.zap.arenaapi.pathfind;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface PathOperation {
    enum State {
        INCOMPLETE,
        SUCCEEDED,
        FAILED
    }

    boolean step(@NotNull PathfinderContext context);

    @NotNull PathOperation.State getState();

    @NotNull PathResult getResult();

    int desiredIterations();

    boolean shouldRemove();

    @NotNull World getWorld();

    @NotNull Set<PathDestination> getDestinations();
}