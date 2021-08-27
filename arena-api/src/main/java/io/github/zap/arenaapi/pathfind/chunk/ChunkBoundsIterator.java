package io.github.zap.arenaapi.pathfind.chunk;

import io.github.zap.vector.Vector2I;
import io.github.zap.vector.Vectors;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class ChunkBoundsIterator implements Iterator<Vector2I> {
    private final int minChunkX;
    private final int maxChunkX;
    private final int maxChunkZ;

    int x;
    int z;

    public ChunkBoundsIterator(@NotNull BoundingBox worldRelativeBounds) {
        Vector min = worldRelativeBounds.getMin();
        Vector max = worldRelativeBounds.getMax();

        minChunkX = min.getBlockX() >> 4;
        maxChunkX = (max.getBlockX() >> 4) + 1;
        maxChunkZ = (max.getBlockZ() >> 4) + 1;

        x = minChunkX - 1;
        z = min.getBlockZ() >> 4;
    }

    @Override
    public boolean hasNext() {
        int nextX = x + 1;
        int nextZ = z;

        if(nextX == maxChunkX) {
            nextZ++;
        }

        return nextZ < maxChunkZ;
    }

    @Override
    public Vector2I next() {
        if(++x == maxChunkX) {
            x = minChunkX;
            z++;
        }

        if(z >= maxChunkZ || x >= maxChunkX) {
            throw new IllegalStateException();
        }

        return Vectors.of(x, z);
    }
}