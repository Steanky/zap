package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector2.ChunkVector;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;


class ChunkRange implements ChunkCoordinateProvider {
    private final class ChunkRangeIterator implements Iterator<ChunkVector> {
        private int x = minX;
        private int z = minZ;

        @Override
        public boolean hasNext() {
            return z + 1 < maxZ || x + 1 < maxX;
        }

        @Override
        public ChunkVector next() {
            if(++x == maxX) {
                x = minX;
                z++;
            }

            return ChunkVector.immutable(x, z);
        }
    }

    public final int minX;
    public final int maxX;
    public final int minZ;
    public final int maxZ;
    private final int hash;

    ChunkRange(int x1, int z1, int x2, int z2) {
        minX = Math.min(x1, x2);
        maxX = Math.max(x1, x2);
        minZ = Math.min(z1, z2);
        maxZ = Math.max(z1, z2);
        hash = Objects.hash(minX, maxX, minZ, maxZ);
    }

    ChunkRange(@NotNull ChunkVector first, @NotNull ChunkVector second) {
        this(first.chunkX(), first.chunkZ(), second.chunkX(), second.chunkZ());
    }

    ChunkRange(@NotNull ChunkVector center, int radius) {
        this(center.chunkX() - radius, center.chunkZ() - radius, center.chunkX() + radius, center.chunkZ() + radius);
    }

    public boolean hasChunk(int x, int z) {
        return x >= minX && x < maxX && z >= minZ && z < maxZ;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ChunkRange) {
            ChunkRange other = (ChunkRange) obj;
            return minX == other.minX && maxX == other.maxX && minZ == other.minZ && maxZ == other.maxZ;
        }

        return false;
    }

    @NotNull
    @Override
    public Iterator<ChunkVector> iterator() {
        return new ChunkRangeIterator();
    }
}