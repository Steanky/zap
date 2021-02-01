package io.github.zap.arenaapi.particle;

/**
 * Interface for an object that provides an array of fragments.
 */
public interface Renderable {
    /**
     * Gets the array of fragments.
     * @return The array of fragments that can be used to visually render this object
     */
    FragmentData[] getFragments();
}
