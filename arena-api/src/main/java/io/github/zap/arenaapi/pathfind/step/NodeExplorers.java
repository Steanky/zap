package io.github.zap.arenaapi.pathfind.step;

import io.github.zap.arenaapi.pathfind.chunk.ChunkBounds;
import org.jetbrains.annotations.NotNull;

public final class NodeExplorers {
    public static @NotNull NodeExplorer basicWalk(@NotNull NodeStepper stepper, @NotNull ChunkBounds chunkBounds) {
        return new WalkNodeExplorer(stepper, chunkBounds);
    }
}
