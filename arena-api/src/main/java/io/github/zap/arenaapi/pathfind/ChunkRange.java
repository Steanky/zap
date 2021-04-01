package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;


public class ChunkRange implements Iterable<ChunkCoordinate> {
    private final class ChunkRangeIterator implements Iterator<ChunkCoordinate> {
        private int x = minX;
        private int z = minZ;

        @Override
        public boolean hasNext() {
            return z + 1 < maxZ || x + 1 < maxX;
        }

        @Override
        public ChunkCoordinate next() {
            if(++x == maxX) {
                x = minX;
                z++;
            }
            return new ChunkCoordinate(x, z);
        }
    }

    public final int minX;
    public final int maxX;
    public final int minZ;
    public final int maxZ;
    private final int hash;

    public ChunkRange(int x1, int z1, int x2, int z2) {
        minX = Math.min(x1, x2);
        maxX = Math.max(x1, x2);
        minZ = Math.min(z1, z2);
        maxZ = Math.max(z1, z2);
        hash = Objects.hash(minX, maxX, minZ, maxZ);
    }

    public ChunkRange(@NotNull ChunkCoordinate first, @NotNull ChunkCoordinate second) {
        this(first.x, first.z, second.x, second.z);
    }

    public ChunkRange(@NotNull ChunkCoordinate center, int radius) {
        this(center.x - radius, center.z - radius, center.x + radius, center.z + radius);
    }

    public ChunkRange(int worldX, int worldZ, int radius) {
        this(new ChunkCoordinate(worldX, worldZ, true), radius);
    }

    public boolean inRange(int x, int z) {
        return x >= minX && x < maxX && z >= minZ && z < maxZ;
    }

    public boolean inRange(@NotNull ChunkCoordinate coordinate) {
        Objects.requireNonNull(coordinate, "coordinate cannot be null!");
        return inRange(coordinate.x, coordinate.z);
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
    public Iterator<ChunkCoordinate> iterator() {
        return new ChunkRangeIterator();
    }
}
