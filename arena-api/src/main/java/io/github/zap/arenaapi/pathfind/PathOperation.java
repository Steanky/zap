package io.github.zap.arenaapi.pathfind;

import org.bukkit.World;

public interface PathOperation {
    boolean step(PathfinderContext context);

    PathState getState();

    PathResult getResult();

    int desiredIterations();

    boolean shouldRemove();

    World getWorld();

    PathDestination getDestination();
}