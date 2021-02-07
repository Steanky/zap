package io.github.zap.arenaapi.particle;

import io.github.zap.arenaapi.BoundedIterator;
import org.bukkit.util.Vector;

public abstract class CachingRenderable implements Renderable {
    private FragmentData[] frags;

    @Override
    public FragmentData[] getFragments() {
        return frags;
    }

    @Override
    public void update() {
        BoundedIterator<Vector> positionIterator = positionIterator();

        int length = positionIterator.getLength();
        FragmentData[] newData = length == frags.length ? frags : new FragmentData[length];

        Shader shader = getShader();
        for(int i = 0; i < newData.length; i++) {
            newData[i] = shader.generateFragment(positionIterator.next());
        }
    }

    public abstract Shader getShader();

    public abstract BoundedIterator<Vector> positionIterator();
}
