package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * General interface for objects that can provide chunk coordinates.
 */
public interface ChunkVectorSource {
    /**
     * Returns the x-coordinate of the chunk this object points to. This may require a conversion.
     * @return The x-coordinate of the chunk this object points to
     */
    int chunkX();

    /**
     * Returns the z-coordinate of the chunk this object points to. This may require a conversion.
     * @return The z-coordinate of the chunk this object points to
     */
    int chunkZ();

    static @NotNull ChunkVectorSource fromChunkCoordinate(int x, int z) {
        return new ChunkVectorSourceImpl(x, z);
    }

    static @NotNull ChunkVectorSource fromWorldCoordinate(int x, int z) {
        return new ChunkVectorSourceImpl(x >> 4, z >> 4);
    }

    static @NotNull ChunkVectorSource fromWorldVector(@NotNull Vector vector) {
        return fromWorldCoordinate(vector.getBlockX(), vector.getBlockZ());
    }
}
