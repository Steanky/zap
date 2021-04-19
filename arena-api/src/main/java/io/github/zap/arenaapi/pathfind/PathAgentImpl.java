package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector.WorldVector;
import org.jetbrains.annotations.NotNull;

class PathAgentImpl implements PathAgent {
    private final Characteristics characteristics;
    private final WorldVector source;

    PathAgentImpl(@NotNull Characteristics characteristics, @NotNull WorldVector source) {
        this.characteristics = characteristics;
        this.source = source;
    }

    @Override
    public @NotNull Characteristics characteristics() {
        return characteristics;
    }

    @Override
    public @NotNull WorldVector position() {
        return source;
    }
}
