package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector.WorldVectorSource;
import org.jetbrains.annotations.NotNull;

class PathAgentImpl implements PathAgent {
    private final Characteristics characteristics;
    private final WorldVectorSource source;

    PathAgentImpl(@NotNull Characteristics characteristics, @NotNull WorldVectorSource source) {
        this.characteristics = characteristics;
        this.source = source;
    }

    @Override
    public @NotNull Characteristics characteristics() {
        return characteristics;
    }

    @Override
    public @NotNull WorldVectorSource position() {
        return source;
    }
}
