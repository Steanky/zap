package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector.ChunkVectorAccess;
import io.github.zap.arenaapi.vector.VectorAccess;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface ChunkCoordinateProvider extends Iterable<ChunkVectorAccess> {
    boolean hasChunk(int x, int z);

    static ChunkCoordinateProvider boundedSquare(@NotNull ChunkVectorAccess from, @NotNull ChunkVectorAccess to) {
        Objects.requireNonNull(from, "from cannot be null!");
        Objects.requireNonNull(to, "to cannot be null!");
        return new ChunkRange(from, to);
    }

    static ChunkCoordinateProvider squareFromCenter(@NotNull VectorAccess center, int radius) {
        Objects.requireNonNull(center, "center cannot be null!");
        Validate.isTrue(radius > 0, "radius cannot be negative!");
        return new ChunkRange(center, radius);
    }
}