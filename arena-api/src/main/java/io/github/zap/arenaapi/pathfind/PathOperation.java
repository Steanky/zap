package io.github.zap.arenaapi.pathfind;

import net.kyori.adventure.sound.Sound;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

public interface PathOperation {
    boolean step(@NotNull PathfinderContext context);

    @NotNull PathState getState();

    @NotNull PathResult getResult();

    int desiredIterations();

    boolean shouldRemove();

    @NotNull World getWorld();

    @NotNull PathDestination getDestination();

    /**
     * Creates a new PathOperation object for the specified mob, leading to the specified destination.
     * @param mob
     * @param destination
     * @return
     */
    static @NotNull PathOperation forMob(@NotNull Mob mob, @NotNull PathDestination destination) {
        return new PathOperationImpl(mob, destination);
    }
}