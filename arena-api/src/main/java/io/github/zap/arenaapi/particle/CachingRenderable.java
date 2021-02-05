package io.github.zap.arenaapi.particle;

import org.bukkit.util.Vector;

public abstract class CachingRenderable implements Renderable {
    private FragmentData[] frags;

    @Override
    public FragmentData[] getFragments() {
        return frags;
    }

    @Override
    public void update() {
        FragmentData[] newData = getShader().generateFragments(calculateLocations(frags));

        /*
        only update the fragment array if necessary; if it's the same object then the call to draw() made no or direct
        changes. this makes certain kinds of animations (those that don't require the use of a new array) much more
        optimized
         */
        if(newData != frags) {
            frags = newData;
        }
    }

    public abstract Vector[] calculateLocations(FragmentData[] previous);
}
