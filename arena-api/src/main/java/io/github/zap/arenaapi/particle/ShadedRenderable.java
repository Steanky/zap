package io.github.zap.arenaapi.particle;

/**
 * Higher-level implementation of Renderable. Performs caching where possible; supports very basic shader-like features
 */
public abstract class ShadedRenderable implements Renderable {
    private FragmentData[] frags = new FragmentData[0];

    public ShadedRenderable() {
        //call update once when object is created; ensures we have some initial values
        update();
    }

    @Override
    public FragmentData[] getFragments() {
        return frags;
    }

    @Override
    public void update() {
        VectorProvider provider = vectorProvider();

        if(provider != null) {
            int length = provider.init();

            //optimization: only allocate a new array if we have to
            FragmentData[] newData = (frags == null || length != frags.length) ? new FragmentData[length] : frags;

            Shader shader = getShader();
            for(int i = 0; i < newData.length; i++) {
                newData[i] = shader.generateFragment(provider.next());
            }

            provider.reset();
            frags = newData;
        }
    }

    public abstract Shader getShader();

    public abstract VectorProvider vectorProvider();
}