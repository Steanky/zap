package io.github.zap.arenaapi.particle;

/**
 * Simple interface objects that need to be visually rendered should extend.
 */
public interface RenderableProvider {
    Renderable getRenderable();
}
