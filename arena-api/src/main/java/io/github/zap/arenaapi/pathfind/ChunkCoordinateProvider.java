package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector2.ChunkVector;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface ChunkCoordinateProvider extends Iterable<ChunkVector> {
    boolean hasChunk(int x, int z);

    static ChunkCoordinateProvider boundedSquare(@NotNull ChunkVector from, @NotNull ChunkVector to) {
        Objects.requireNonNull(from, "from cannot be null!");
        Objects.requireNonNull(to, "to cannot be null!");
        return new ChunkRange(from, to);
    }

    static ChunkCoordinateProvider squareFromCenter(@NotNull ChunkVector center, int radius) {
        Objects.requireNonNull(center, "from cannot be null!");
        Validate.isTrue(radius > 0, "radius cannot be negative!");
        return new ChunkRange(center, radius);
    }

    static ChunkCoordinateProvider squareFromAgent(@NotNull PathAgent agent, int radius) {
        Objects.requireNonNull(agent, "agent cannot be null!");
        Validate.isTrue(radius > 0, "radius cannot be negative!");

        return new ChunkRange(agent.position(), radius);
    }
}
