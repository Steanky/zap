package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.ImmutableWorldVector;
import io.github.zap.vector.VectorAccess;
import org.jetbrains.annotations.NotNull;

class PathAgentImpl extends PathAgent {
    private final Characteristics characteristics;

    PathAgentImpl(@NotNull Characteristics characteristics, @NotNull ImmutableWorldVector vector) {
        super(VectorAccess.immutable(vector.blockX(), vector.y(), vector.blockZ()));
        this.characteristics = characteristics;
    }

    @Override
    public @NotNull Characteristics characteristics() {
        return characteristics;
    }
}
