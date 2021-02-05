package io.github.zap.arenaapi.particle;

import org.bukkit.util.Vector;

/**
 * Interface for an object that provides renderable fragment data.
 */
public interface Renderable {
    /**
     * Gets the segment used to iterate over array fragments
     * @return The ArraySegment containing this renderable's FragmentData
     */
    FragmentData[] getFragments();

    /**
     * Updates the array of fragments displayed by this renderer.
     */
    void update();

    Shader getShader();
}
