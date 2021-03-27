package io.github.zap.arenaapi.pathfind;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface PathOperation {
    boolean step(@NotNull PathfinderContext context);

    @NotNull PathState getState();

    @NotNull PathResult getResult();

    int desiredIterations();

    boolean shouldRemove();

    @NotNull World getWorld();

    @NotNull Set<PathDestination> getDestinations();


    /**
     * Creates a new PathOperation object for the specified mob, leading to the specified destination. PathOperation
     * objects will not do anything themselves; rather, they are processed by PathfinderEngine.
     * @param mob The mob that's trying to pathfind
     * @param destination The destination to try and reach
     * @return The PathOperation object, which can be passed to PathfinderEngine
     */

    /*
    static @NotNull PathOperation forMob(@NotNull Mob mob, @NotNull PathDestination destination, int tolerance) {
        return new PathOperationAdapted(mob, ImmutableList.of(destination), tolerance);
    }*/
}