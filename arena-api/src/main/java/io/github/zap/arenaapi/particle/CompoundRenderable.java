package io.github.zap.arenaapi.particle;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Renderable that implements a basic caching scheme; data can be invalidated, which will force a
 * recalculation when getFragments() is called. Otherwise, the stored array will be continually reused.
 */
public abstract class CompoundRenderable implements Renderable {
    private FragmentData[] fragments;

    private final List<Integer> componentIndices = new ArrayList<>();
    private final List<Integer> invalidatedComponents = new ArrayList<>();

    public FragmentData[] getFragments() {
        int invalidIndicesSize = invalidatedComponents.size();

        for(int i = invalidIndicesSize - 1; i >= 0; i--) {
            int componentIndex = invalidatedComponents.get(i);
            int component = componentIndices.get(componentIndex);
            int length = calculateComponentLength(component, componentIndex);



            invalidatedComponents.remove(i);
        }


        return fragments;
    }

    private int calculateComponentLength(int component, int componentIndex) {
        int size = componentIndices.size();
        int next = componentIndex + 1;

        return (next < size ? componentIndices.get(next) : size) - component;
    }

    /**
     * Invalidates the cache, which will cause some of the fragments for this renderable to be recalculated. It is up
     * to the implementation to decide how this recalculation occurs. In general, the provided range of values should
     * indicate a range of vectors that need to be updated.
     */
    private void invalidate(int start, int length) {
        if(start < 0 || length < 1) {
            throw new IndexOutOfBoundsException(String.format("invalidation range out of bounds: %s, length %s",
                    start, length));
        }
    }

    /**
     * Calculates the fragments for this Renderable. Will be called whenever the cache is invalidated (is initially
     * set invalid).
     * @return The array of fragments that will be stored and used to render this object
     */
    public abstract UpdateResult calculateFragments(int startInclusive, int length);
}
