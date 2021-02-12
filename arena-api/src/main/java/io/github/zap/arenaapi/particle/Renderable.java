package io.github.zap.arenaapi.particle;

/**
 * Interface for an object that provides renderable fragment data.
 */
public interface Renderable {
    /**
     * Gets the segment used to iterate over array fragments. Should generally return a cached value, whose contents
     * are updated when update() is invoked
     * @return The ArraySegment containing this renderable's FragmentData
     */
    FragmentData[] getFragments();

    /**
     * Updates the array of fragments displayed by this renderer.
     */
    void update();
}
