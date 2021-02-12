package io.github.zap.arenaapi.particle;

import org.bukkit.util.Vector;

/**
 * Object that provides a sequence of vectors, in order.
 */
public interface VectorProvider {
    VectorProvider EMPTY = new VectorProvider() {
        @Override
        public int init() {
            return 0;
        }

        @Override
        public Vector next() {
            return null;
        }

        @Override
        public void reset() {

        }
    };

    /**
     * Initializes the VectorProvider, performing necessary tasks so that it may be iterated. The value returned is the
     * number of Vectors this instance provides. In general, calling this multiple times should not have adverse
     * effects.
     * @return The number of vectors this instance will iterate
     */
    int init();

    /**
     * Gets the next vector. Can throw an exception if it is called more times than length allows, but is not required
     * to do so in order to allow for faster iteration.
     * @return The next vector
     */
    Vector next();

    /**
     * Resets the state of the object, so it can be iterated again.
     */
    void reset();
}
