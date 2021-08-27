package io.github.zap.arenaapi.pathfind.path;

import io.github.zap.vector.Vector3I;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PathNodeFactory<T extends PathNode> {
    @NotNull T make(@NotNull Vector3I vector);
}
