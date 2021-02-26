package io.github.zap.arenaapi.particle;

/**
 * Interface for an object that provides renderable fragment data.
 */
public interface Renderable {
    /**
     * Gets the array of fragments that the renderer should use. This function is called very often and it should
     * return a cached value whenever possible.
     * @return The array of fragment data representing this Renderable
     */
    FragmentData[] getFragments();

    /**
     * Updates the array of fragments displayed by this renderer.
     */
    void update();
}
