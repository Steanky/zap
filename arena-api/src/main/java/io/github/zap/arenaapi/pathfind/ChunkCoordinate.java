package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ChunkCoordinate implements ChunkVectorSource {
    public final int x;
    public final int z;
    private final int hash;

    public ChunkCoordinate(int x, int z) {
        this.x = x;
        this.z = z;
        hash = Objects.hash(x, z);
    }

    public ChunkCoordinate(@NotNull ChunkVectorSource chunkCoordinates) {
        this(chunkCoordinates.chunkX(), chunkCoordinates.chunkZ());
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ChunkCoordinate) {
            ChunkCoordinate other = (ChunkCoordinate) obj;
            return x == other.x && z == other.z;
        }

        return false;
    }

    @Override
    public String toString() {
        return "ChunkCoordinate{x=" + x + ", z=" + z + "}";
    }

    @Override
    public int chunkX() {
        return x;
    }

    @Override
    public int chunkZ() {
        return z;
    }
}
