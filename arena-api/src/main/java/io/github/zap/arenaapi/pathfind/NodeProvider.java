package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

public interface NodeProvider {
    NodeProvider SIMPLE = new NodeProvider() {
        @Override
        public PathNode[] generateNodes(@NotNull PathfinderContext context, @NotNull PathOperation operation, @NotNull PathNode nodeAt) {

            return new PathNode[0];
        }
    };

    PathNode[] generateNodes(@NotNull PathfinderContext context, @NotNull PathOperation operation, @NotNull PathNode nodeAt);
}
