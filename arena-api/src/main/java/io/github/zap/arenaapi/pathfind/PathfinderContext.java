package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface PathfinderContext {
    @NotNull PathfinderEngine engine();

    @NotNull BlockCollisionProvider blockProvider();
}
