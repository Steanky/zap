package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.nms.common.world.BlockSnapshot;
import io.github.zap.vector.ChunkVectorAccess;
import io.github.zap.vector.VectorAccess;
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
    @NotNull World getWorld();

    /**
     * Returns whether or not this BlockCollisionProvider supports asynchronous access; that is, access by threads other
     * than the main server. This does not guarantee any sort of safety in regards to concurrent access.
     * @return True if this objects supports access off of the main server thread, false otherwise
     */
    boolean supportsAsync();

    void updateRegion(@NotNull ChunkCoordinateProvider coordinates);

    @Nullable BlockSnapshot getBlock(int x, int y, int z);

    boolean collidesWithAnySolid(@NotNull BoundingBox bounds);

    @NotNull List<BlockSnapshot> collidingSolids(@NotNull BoundingBox bounds);

    default @Nullable BlockSnapshot getBlock(@NotNull VectorAccess at) {
        return getBlock(at.blockX(), at.blockY(), at.blockZ());
    }
}