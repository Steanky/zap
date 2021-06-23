package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector.ChunkVectorAccess;
import io.github.zap.arenaapi.vector.VectorAccess;
import io.github.zap.nms.common.world.BlockCollisionSnapshot;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * This interface provides a general template for a class which provides block state information over a limited
 * region in a single dimension. If implementations are to be used with asynchronous PathfinderEngines, certain methods
 * must, at the very least, allow safe access from threads other than the main server thread. It is NOT necessary for
 * implementations to support concurrency in any capacity, and may throw exceptions when multiple threads attempt to
 * call a method concurrently.
 */
public interface BlockCollisionProvider {
    /**
     * Returns the World object this BlockProvider is linked to.
     * @return The Bukkit World this BlockProvider uses. Operations on this object are generally not thread-safe.
     */
    @NotNull World getWorld();

    /**
     * Clears all of the chunks associated with the given world.
     * @param worldUID The world UUID to remove chunks for
     */
    void clearChunksFor(@NotNull UUID worldUID);

    /**
     * Returns whether or not this BlockCollisionProvider supports asynchronous access; that is, access by threads other
     * than the main server. This does not guarantee any sort of safety in regards to concurrent access.
     * @return True if this objects supports access off of the main server thread, false otherwise
     */
    boolean supportsAsync();

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