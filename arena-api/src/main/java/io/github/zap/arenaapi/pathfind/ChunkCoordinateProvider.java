package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector.ChunkVectorSource;
import io.github.zap.arenaapi.vector.WorldVectorSource;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface ChunkCoordinateProvider extends Iterable<ChunkVectorSource> {
    boolean hasChunk(int x, int z);

    static ChunkCoordinateProvider bounded(@NotNull ChunkVectorSource from, @NotNull ChunkVectorSource to) {
        Objects.requireNonNull(from, "from cannot be null!");
        Objects.requireNonNull(to, "to cannot be null!");
        return new ChunkRange(from, to);
    }

    static ChunkCoordinateProvider fromCenter(@NotNull ChunkVectorSource center, int radius) {
        Objects.requireNonNull(center, "from cannot be null!");
        Validate.isTrue(radius > 0, "radius cannot be negative!");
        return new ChunkRange(center, radius);
    }

    static ChunkCoordinateProvider fromAgent(@NotNull PathAgent agent, int radius) {
        Objects.requireNonNull(agent, "agent cannot be null!");
        Validate.isTrue(radius > 0, "radius cannot be negative!");

        WorldVectorSource position = agent.position();
        return new ChunkRange(position, radius);
    }
}
