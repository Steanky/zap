package io.github.zap.arenaapi.particle;

/**
 * Higher-level implementation of Renderable. Performs caching where possible; supports very basic shader-like features
 */
public abstract class ShadedRenderable implements Renderable {
    private FragmentData[] frags;

    @Override
    public FragmentData[] getFragments() {
        return frags;
    }

    @Override
    public void update() {
        VectorProvider provider = vectorProvider();

        int length = provider.init();
        FragmentData[] newData = length == frags.length ? frags : new FragmentData[length];

        Shader shader = getShader();
        for(int i = 0; i < newData.length; i++) {
            newData[i] = shader.generateFragment(provider.next());
        }
    }

    public abstract Shader getShader();

    public abstract VectorProvider vectorProvider();
}