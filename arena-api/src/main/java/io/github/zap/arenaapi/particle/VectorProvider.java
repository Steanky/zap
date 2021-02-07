package io.github.zap.arenaapi.particle;

import org.bukkit.util.Vector;

/**
 * Object that provides a sequence of vectors.
 */
public interface VectorProvider {
    /**
     * Initializes the VectorProvider. This will allocate space. Returns the number of vectors that will be returned
     * by this instance. Must ALWAYS be called before next()!
     * @return The number of vectors
     */
    int init();

    /**
     * Gets the next vector. Can throw an exception if it is called more times than length allows.
     * @return The next vector
     */
    Vector next();

    /**
     * Resets the state of the object, so it can be created again.
     */
    void reset();
}
