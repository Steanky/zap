package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector.WorldVector;
import org.jetbrains.annotations.NotNull;

class PathAgentImpl extends PathAgent {
    private final Characteristics characteristics;

    PathAgentImpl(@NotNull Characteristics characteristics, @NotNull WorldVector source) {
        super(source.worldX(), source.worldY(), source.worldZ());
        this.characteristics = characteristics;
    }

    @Override
    public @NotNull Characteristics characteristics() {
        return characteristics;
    }
}
