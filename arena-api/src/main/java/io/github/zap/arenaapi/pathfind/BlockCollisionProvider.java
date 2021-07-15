package io.github.zap.arenaapi.pathfind;

import io.github.zap.nms.common.world.BlockSnapshot;
import io.github.zap.nms.common.world.CollisionChunkSnapshot;
import io.github.zap.vector.Vector3I;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
    @NotNull World world();

    /**
     * Returns whether or not this BlockCollisionProvider supports asynchronous access; that is, access by threads other
     * than the main server. This does not guarantee any sort of safety in regards to concurrent access.
     * @return True if this objects supports access off of the main server thread, false otherwise
     */
    boolean supportsAsync();

    void updateRegion(@NotNull ChunkCoordinateProvider coordinates);

    void clearRegion(@NotNull ChunkCoordinateProvider coordinates);

    void clearForWorld();

    boolean hasChunk(int x, int z);

    @Nullable CollisionChunkSnapshot chunkAt(int x, int z);

    @Nullable BlockSnapshot getBlock(int x, int y, int z);

    boolean collidesWithAny(@NotNull BoundingBox bounds);

    @NotNull List<BlockSnapshot> collidingSolids(@NotNull BoundingBox bounds);

    default @Nullable BlockSnapshot getBlock(@NotNull Vector3I at) {
        return getBlock(at.x(), at.y(), at.z());
    }
}