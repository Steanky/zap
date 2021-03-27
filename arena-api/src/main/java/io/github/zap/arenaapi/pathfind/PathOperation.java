package io.github.zap.arenaapi.pathfind;

import net.minecraft.server.v1_16_R3.PathEntity;

public interface PathOperation {
    boolean step(PathfinderContext context);

    PathEntity getPath();

    PathState getState();

    int desiredIterations();

    int incrementAge();
}