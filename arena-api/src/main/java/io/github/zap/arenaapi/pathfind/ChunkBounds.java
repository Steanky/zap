package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.ChunkVectorAccess;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;

public class ChunkBounds implements ChunkCoordinateProvider {
    private final class ChunkRangeIterator implements Iterator<ChunkVectorAccess> {
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
        public ChunkVectorAccess next() {
            if(++x == maxX) {
                x = minX;
                z++;
            }

            return ChunkVectorAccess.immutable(x, z);
        }
    }

    private final int minX;
    private final int minZ;

    private final int maxX;
    private final int maxZ;

    private final int width;
    private final int height;

    private final int hash;

    public ChunkBounds(int x1, int z1, int x2, int z2) {
        minX = Math.min(x1, x2);
        minZ = Math.min(z1, z2);

        maxX = Math.max(x1, x2);
        maxZ = Math.max(z1, z2);

        width = maxX - minX;
        height = maxZ - minZ;

        hash = Objects.hash(minX, maxX, minZ, maxZ);
    }

    public ChunkBounds(@NotNull ChunkVectorAccess first, @NotNull ChunkVectorAccess second) {
        this(first.chunkX(), first.chunkZ(), second.chunkX(), second.chunkZ());
    }

    public ChunkBounds(@NotNull ChunkVectorAccess center, int radius) {
        this(center.chunkX() - radius - 1, center.chunkZ() - radius - 1, center.chunkX() + radius, center.chunkZ() + radius);
    }

    public boolean hasChunk(int x, int z) {
        return x >= minX && x < maxX && z >= minZ && z < maxZ;
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
        return hash;
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
    public Iterator<ChunkVectorAccess> iterator() {
        return new ChunkRangeIterator();
    }
}