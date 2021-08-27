package io.github.zap.arenaapi.pathfind.step;

import io.github.zap.arenaapi.pathfind.chunk.ChunkCoordinateProvider;
import org.jetbrains.annotations.NotNull;

public final class NodeExplorers {
    public static @NotNull NodeExplorer basicWalk(@NotNull NodeStepper stepper, @NotNull ChunkCoordinateProvider chunkCoordinateProvider) {
        return new WalkNodeExplorer(stepper, chunkCoordinateProvider);
    }
}
