package io.github.zap.arenaapi.pathfind;

import net.minecraft.server.v1_16_R3.PathEntity;
import org.bukkit.World;

public interface PathOperation {
    boolean step(PathfinderContext context);

    PathEntity getPathEntity();

    PathState getState();

    PathResult getResult();

    int desiredIterations();

    int incrementAge();

    World getWorld();
}