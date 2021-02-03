package io.github.zap.arenaapi.particle;

import io.github.zap.arenaapi.util.ArraySegment;

/**
 * Interface for an object that provides renderable fragment data.
 */
public interface Renderable {
    /**
     * Gets the segment used to iterate over array fragments
     * @return The ArraySegment containing this renderable's FragmentData
     */
    ArraySegment<FragmentData> getFragments();
}
