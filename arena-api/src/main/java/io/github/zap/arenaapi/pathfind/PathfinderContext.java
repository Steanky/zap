package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

public interface PathfinderContext {
    @NotNull PathfinderEngine engine();

    @NotNull BlockCollisionProvider blockProvider();
}
