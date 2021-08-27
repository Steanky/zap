package io.github.zap.arenaapi.pathfind.path;

import io.github.zap.vector.Vector3I;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PathNode extends Vector3I {
    void setOffsetVector(@NotNull Vector3I vector);

    @NotNull Vector3I getOffsetVector();

    @Nullable PathNode parent();
}
