package io.github.zap.arenaapi.particle;

/**
 * Simple interface objects that need to be visually rendered should extend.
 */
public interface RenderableProvider {
    /**
     * Gets the Renderable instance for this object. In general, this should cache its return value and
     * maintain that single Renderable instance for the RenderableProvider's lifetime.
     * @return The renderable instance for this provider
     */
    Renderable getRenderable();
}
