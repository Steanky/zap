package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.Vector2I;
import io.github.zap.vector.Vector3I;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface ChunkCoordinateProvider extends Iterable<Vector2I> {
    boolean hasChunk(int x, int z);

    boolean hasBlock(int x, int y, int z);

    default boolean hasBlock(@NotNull Vector3I block) {
        return hasBlock(block.x(), block.y(), block.z());
    }

    int chunkCount();

    int width();

    int height();

    int minX();

    int maxX();

    int minZ();

    int maxZ();

    static ChunkCoordinateProvider boundedSquare(@NotNull Vector2I from, @NotNull Vector2I to) {
        Objects.requireNonNull(from, "from cannot be null!");
        Objects.requireNonNull(to, "to cannot be null!");
        return new ChunkBounds(from, to);
    }

    static ChunkCoordinateProvider squareFromCenter(@NotNull Vector2I center, int radius) {
        Objects.requireNonNull(center, "center cannot be null!");
        Validate.isTrue(radius > 0, "radius cannot be negative!");
        return new ChunkBounds(center, radius);
    }
}
