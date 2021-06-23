package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector.ChunkVectorAccess;
import io.github.zap.arenaapi.vector.VectorAccess;
import io.github.zap.nms.common.world.BlockCollisionSnapshot;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This interface provides a general template for a class which provides block state information over a limited
 * region in a single dimension. Implementations must provide a certain degree of thread safety, if they are expected
 * to be used with asynchronous PathfinderEngines.
 */
public interface BlockProvider {
    /**
     * Returns the World object this BlockProvider is linked to.
     * @return The Bukkit World this BlockProvider uses. Operations on this object are generally not thread-safe.
     */
    @NotNull World getWorld();

    boolean hasChunkAt(int x, int y);

    void updateChunk(int x, int y);

    default void updateChunk(@NotNull ChunkVectorAccess vector) {
        updateChunk(vector.chunkX(), vector.chunkZ());
    }

    void updateAll();

    @NotNull ChunkCoordinateProvider coordinateProvider();

    @Nullable BlockCollisionSnapshot getBlock(int x, int y, int z);

    boolean collisionAt(double x, double y, double z);

    default @Nullable BlockCollisionSnapshot getBlock(@NotNull VectorAccess at) {
        return getBlock(at.blockX(), at.blockY(), at.blockZ());
    }
}