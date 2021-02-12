package io.github.zap.arenaapi.particle;

/**
 * Simple interface for an object that can render Renderables.
 */
public interface Renderer {
    void start();

    void stop();

    /**
     * Renders any renderables this instance manages.
     */
    void draw();

    Renderable get(int index);

    /**
     * Registers a renderable with this Renderer.
     * @param renderable The Renderable to register
     */
    void add(Renderable renderable);

    void remove(int index);

    void set(int index, Renderable value);

    int size();
}
