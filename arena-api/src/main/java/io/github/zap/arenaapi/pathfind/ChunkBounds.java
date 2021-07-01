package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.ChunkVectorAccess;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;

public class ChunkBounds implements ChunkCoordinateProvider {
    private final class ChunkRangeIterator implements Iterator<ChunkVectorAccess> {
        private int x = minX;
        private int z = minZ;

        @Override
        public boolean hasNext() {
            return z < maxZ;
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
    private final int maxX;
    private final int minZ;
    private final int maxZ;

    private final int hash;

    public ChunkBounds(int x1, int z1, int x2, int z2) {
        minX = Math.min(x1, x2);
        maxX = Math.max(x1, x2) + 1;

        minZ = Math.min(z1, z2);
        maxZ = Math.max(z1, z2) + 1;

        hash = Objects.hash(minX, maxX, minZ, maxZ);
    }

    public ChunkBounds(@NotNull ChunkVectorAccess first, @NotNull ChunkVectorAccess second) {
        this(first.chunkX(), first.chunkZ(), second.chunkX(), second.chunkZ());
    }

    public ChunkBounds(@NotNull ChunkVectorAccess center, int radius) {
        this(center.chunkX() - radius, center.chunkZ() - radius, center.chunkX() + radius, center.chunkZ() + radius);
    }

    public boolean hasChunk(int x, int z) {
        return x >= minX && x < maxX && z >= minZ && z < maxZ;
    }

    @Override
    public int chunkCount() {
        return (maxX - minX) * (maxZ - minZ);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ChunkBounds) {
            ChunkBounds other = (ChunkBounds) obj;
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