package io.github.zap.arenaapi.particle;

import io.github.zap.arenaapi.util.ArraySegment;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Acts as a object inside which other Renderable objects are contained. Performs fragment caching; the
 * underlying array will only be updated as necessary. Resizes similarly to ArrayList, but unlike ArrayList the
 * array may also shrink to reduce memory under certain circumstances, as well as grow.
 */
public class CompoundRenderable implements Renderable {
    private static final int DEFAULT_INITIAL_CAPACITY = 100;
    private static final double DEFAULT_LOAD_FACTOR = 0.75;

    @AllArgsConstructor
    private static class RenderableEntry {
        private final Renderable renderable;
        private int fragmentIndex;
        private int length;
    }

    private final double loadFactor;
    private FragmentData[] fragments;
    private int length;

    private final List<RenderableEntry> renderables = new ArrayList<>();

    public CompoundRenderable(int initialCapacity, double loadFactor) {
        fragments = new FragmentData[initialCapacity];
        this.loadFactor = loadFactor;
    }

    public CompoundRenderable(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public CompoundRenderable(double loadFactor) {
        this(DEFAULT_INITIAL_CAPACITY, loadFactor);
    }

    public CompoundRenderable() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    @Override
    public ArraySegment<FragmentData> getFragments() {
        return new ArraySegment<>(fragments, length, 0);
    }

    /**
     * Adds a renderable to this instance. If necessary, the backing array may be resized.
     * @param renderable The renderable to add
     */
    public void addRenderable(Renderable renderable) {
        ArraySegment<FragmentData> newFragments = renderable.getFragments();
        int newFragmentsLength = newFragments.getLength();

        ensureCapacity(newFragmentsLength);
        newFragments.copyTo(0, fragments, length, newFragmentsLength);

        renderables.add(new RenderableEntry(renderable, length, newFragmentsLength));
        length += newFragmentsLength;
    }

    /**
     * Recalculates the Renderable located at the given index. Will resize the array as-needed, including shrinking it
     * if too many unused indices exist.
     * @param index The Renderable to update
     */
    public void updateRenderable(int index) {
        RenderableEntry target = renderables.get(index);
        ArraySegment<FragmentData> newFragments = target.renderable.getFragments();
        int newFragmentsLength = newFragments.getLength();

        if(newFragmentsLength == target.length) {
            newFragments.copyTo(0, fragments, target.fragmentIndex, newFragmentsLength);
        }
        else {
            int diff = newFragmentsLength - target.length;

            //grow the backing array if necessary
            ensureCapacity(diff);

            if(index < renderables.size() - 1) { //we must shift some elements that are forward of us
                int nextRenderable = target.fragmentIndex + target.length;
                System.arraycopy(fragments, nextRenderable, fragments, nextRenderable + diff,
                        length - target.fragmentIndex - target.length);

                //update fragment indices for shifted renderables
                for(int i = index + 1; i < renderables.size(); i++) {
                    renderables.get(i).fragmentIndex += diff;
                }
            }

            newFragments.copyTo(0, fragments, target.fragmentIndex, newFragmentsLength);

            target.length = newFragmentsLength;
            length += diff;

            tryShrink();
        }
    }

    public void removeRenderable(int index) {
        RenderableEntry target = renderables.get(index);
        if(target.length > 0 && index < renderables.size() - 1) {
            System.arraycopy(fragments, target.fragmentIndex + target.length, fragments, target.fragmentIndex,
                    length - target.fragmentIndex - target.length);

            //shift indices back
            for(int i = index + 1; i < renderables.size(); i++) {
                renderables.get(i).fragmentIndex -= target.length;
            }

            length -= target.length;

            tryShrink();
        }

        renderables.remove(index);
    }

    public Renderable getRenderable(int index) {
        return renderables.get(index).renderable;
    }

    private void tryShrink() {
        if(length < fragments.length * loadFactor) {
            FragmentData[] newFragments = new FragmentData[length];
            System.arraycopy(fragments, 0, newFragments, 0, length);
            fragments = newFragments;
        }
    }

    private void ensureCapacity(int incoming) {
        if(length + incoming > fragments.length) {
            //increase size by the necessary amount
            grow((length + incoming) - fragments.length);
        }
    }

    private void grow(int by) {
        FragmentData[] newArray = new FragmentData[fragments.length + by];
        System.arraycopy(fragments, 0, newArray, 0, fragments.length);
        fragments = newArray;
    }
}