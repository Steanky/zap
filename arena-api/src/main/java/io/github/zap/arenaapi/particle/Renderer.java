package io.github.zap.arenaapi.particle;

/**
 * Simple interface for an object that can render an array of FragmentData.
 */
public interface Renderer {
    /**
     * Renders the provided array of FragmentData.
     * @param data The data to render
     */
    void draw(FragmentData[] data);
}
