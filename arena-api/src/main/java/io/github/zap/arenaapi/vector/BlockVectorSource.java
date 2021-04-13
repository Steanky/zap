package io.github.zap.arenaapi.vector;

import io.github.zap.arenaapi.util.VectorUtils;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BlockVectorSource extends ChunkVectorSource {
    private final int x;
    private final int y;
    private final int z;
    private int hash = -1;

    private Integer chunkX = null;
    private Integer chunkZ = null;

    public BlockVectorSource(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockVectorSource(@NotNull Vector vector) {
        x = vector.getBlockX();
        y = vector.getBlockY();
        z = vector.getBlockZ();
    }

    @Override
    public int chunkX() {
        return chunkX == null ? chunkX = x >> 4 : chunkX;
    }

    @Override
    public int chunkZ() {
        return chunkZ == null ? chunkZ = z >> 4 : chunkZ;
    }

    public int blockX() {
        return x;
    }

    public int blockY() {
        return y;
    }

    public int blockZ() {
        return z;
    }

    public double blockDistanceSquared(@NotNull BlockVectorSource other) {
        return VectorUtils.distanceSquared(x, y, z, other.x, other.y, other.z);
    }

    public double blockDistance(@NotNull BlockVectorSource other) {
        return VectorUtils.distance(x, y, z, other.x, other.y, other.z);
    }

    @Override
    public int hashCode() {
        return hash == -1 ? hash = Objects.hash(x, y, z) : hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass() == BlockVectorSource.class) {
            BlockVectorSource other = (BlockVectorSource) obj;
            return x == other.x && y == other.y && z == other.z;
        }

        return false;
    }

    @Override
    public String toString() {
        return "BlockVectorSource{x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
