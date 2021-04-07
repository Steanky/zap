package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.Disposable;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * General interface implemented by classes responsible for handling multiple pathfinding operations. Depending on the
 * implementation, these operations may run in parallel (separate from the server thread, on one or more worker
 * threads), or synchronously (on the main server thread).
 *
 * In general, asynchronous implementations should be safe to call from any thread and should properly synchronize to
 * avoid improper concurrent modification. Synchronous implementations may support invocation from threads other than
 * the main server thread, but the general expectation is that they will not.
 *
 * Asynchronous implementations must have their isAsync() method return true. Synchronous implementations must have it
 * return false.
 */
public interface PathfinderEngine extends Disposable {
    /**
     * Offers a PathOperation to this PathfinderEngine. Upon completion, the provided consumer will be called. The
     * thread it is called on depends upon the implementation — asynchronous PathfinderEngines may call off of the
     * server thread. Synchronous PathfinderEngines are guaranteed to always call their consumer on the server thread.
     *
     * This method offers no guarantee as to whether or not the operation will actually be completed (that is, it will
     * call its consumer with a PathResult). For asynchronous operations particularly, pending routines may be
     * terminated if the PathfinderEngine is disposed.
     * @param operation The operation to queue
     * @param world The world in which the operation is taking place
     * @param resultConsumer The Consumer to invoke once the operation has completed (in either a successful or failed
     *                       state).
     */
    void giveOperation(@NotNull PathOperation operation, @NotNull World world, @NotNull Consumer<PathResult> resultConsumer);

    /**
     * Indicates the concurrent behavior of this PathfinderEngine.
     * @return true if the PathfinderEngine is an asynchronous implementation; false if it is synchronous.
     */
    boolean isAsync();

    /**
     * Returns the default asynchronous implementation of PathfinderEngine.
     * @return The PathfinderEngine instance, which will be an asynchronous implementation
     */
    static PathfinderEngine async() {
        return AsyncPathfinderEngine.instance();
    }
}