package io.github.zap.arenaapi.pathfind.chunk;

import io.github.zap.vector.Vector2I;
import io.github.zap.vector.Vectors;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;

public class ChunkBounds implements ChunkCoordinateProvider {
    private class ChunkBoundsIterator implements Iterator<Vector2I> {
        private int x = minX - 1;
        private int z = minZ;

        @Override
        public boolean hasNext() {
            int nextX = x + 1;
            int nextZ = z;

            if(nextX == maxX) {
                nextZ++;
            }

            return nextZ < maxZ;
        }

        @Override
        public Vector2I next() {
            if(++x == maxX) {
                x = minX;
                z++;
            }

            return Vectors.of(x, z);
        }
    }

    private final int minX;
    private final int minZ;

    private final int maxX;
    private final int maxZ;

    private final int width;
    private final int height;

    public ChunkBounds(int x1, int z1, int x2, int z2) {
        minX = Math.min(x1, x2);
        minZ = Math.min(z1, z2);

        maxX = Math.max(x1, x2);
        maxZ = Math.max(z1, z2);

        width = maxX - minX;
        height = maxZ - minZ;
    }

    public ChunkBounds(@NotNull Vector2I first, @NotNull Vector2I second) {
        this(first.x(), first.z(), second.x(), second.z());
    }

    public ChunkBounds(@NotNull Vector2I center, int radius) {
        this(center.x() - radius - 1, center.z() - radius - 1, center.x() + radius, center.z() + radius);
    }

    public boolean hasChunk(int x, int z) {
        return x >= minX && x < maxX && z >= minZ && z < maxZ;
    }

    @Override
    public boolean hasBlock(int x, int y, int z) {
        if(y > -1 && y < 256) {
            int chunkX = x >> 4;
            int chunkZ = z >> 4;

            return hasChunk(chunkX, chunkZ);
        }

        return false;
    }

    @Override
    public int chunkCount() {
        return (maxX - minX) * (maxZ - minZ);
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public int minX() {
        return minX;
    }

    @Override
    public int maxX() {
        return maxX;
    }

    @Override
    public int minZ() {
        return minZ;
    }

    @Override
    public int maxZ() {
        return maxZ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minX, maxX, minZ, maxZ);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ChunkBounds other) {
            return minX == other.minX && maxX == other.maxX && minZ == other.minZ && maxZ == other.maxZ;
        }

        return false;
    }

    @NotNull
    @Override
    public Iterator<Vector2I> iterator() {
        return new ChunkBoundsIterator();
    }
}