package io.github.zap.arenaapi.pathfind;

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
        return new ChunkVectorSource() {
            @Override
            public int chunkX() {
                return x;
            }

            @Override
            public int chunkZ() {
                return z;
            }
        };
    }

    static @NotNull ChunkVectorSource fromWorldCoordinate(int x, int z) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        return new ChunkVectorSource() {
            @Override
            public int chunkX() {
                return chunkX;
            }

            @Override
            public int chunkZ() {
                return chunkZ;
            }
        };
    }

    static @NotNull ChunkVectorSource fromWorldVector(@NotNull Vector vector) {
        return fromWorldCoordinate(vector.getBlockX(), vector.getBlockZ());
    }
}
