package io.github.zap.arenaapi.pathfind.chunk;

import io.github.zap.vector.Vector2I;
import io.github.zap.vector.Vector3I;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface ChunkBounds extends Iterable<Vector2I> {
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
}
