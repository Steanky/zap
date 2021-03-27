package io.github.zap.arenaapi.pathfind;

import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

class PathOperationImpl implements PathOperation {
    private final Mob mob;
    private  final PathDestination destination;

    PathOperationImpl(@NotNull Mob mob, @NotNull PathDestination destination) {
        this.mob = mob;
        this.destination = destination;
    }

    @Override
    public boolean step(@NotNull PathfinderContext context) {
        return false;
    }

    @Override
    public @NotNull PathState getState() {
        return null;
    }

    @Override
    public @NotNull PathResult getResult() {
        return null;
    }

    @Override
    public int desiredIterations() {
        return 0;
    }

    @Override
    public boolean shouldRemove() {
        return false;
    }

    @Override
    public @NotNull World getWorld() {
        return null;
    }

    @Override
    public @NotNull PathDestination getDestination() {
        return null;
    }
}
