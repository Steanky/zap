package io.github.zap.arenaapi.pathfind;

import java.util.Objects;

public class ChunkCoordinate {
    public final int x;
    public final int z;
    private final int hash;

    public ChunkCoordinate(int x, int z, boolean isWorldCoordinate) {
        if(isWorldCoordinate) {
            this.x = x / 16;
            this.z = z / 16;
        }
        else {
            this.x = x;
            this.z = z;
        }

        hash = Objects.hash(x, z);
    }

    public ChunkCoordinate(int x, int z) {
        this(x, z, false);
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
}
