package io.github.zap.arenaapi.particle;

import lombok.RequiredArgsConstructor;

/**
 * Renderable that renders object fields that can be rendered (tagged with Viewable)
 */
@RequiredArgsConstructor
public class ViewRenderable extends ShadedRenderable {
    private final Object target;
    private final Shader shader;

    @Override
    public Shader getShader() {
        return shader;
    }

    @Override
    public VectorProvider vectorProvider() {
        return null;
    }
}
