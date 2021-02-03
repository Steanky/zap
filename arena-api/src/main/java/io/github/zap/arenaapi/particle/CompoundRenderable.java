package io.github.zap.arenaapi.particle;

import io.github.zap.arenaapi.util.ArraySegment;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Acts as a container inside which other Renderable objects may be registered. Performs fragment caching; the
 * underlying array will only be updated as necessary.
 */
public abstract class CompoundRenderable implements Renderable {
    @AllArgsConstructor
    private static class RenderableEntry {
        private final Renderable renderable;
        private int fragmentIndex;
        private int length;
    }

    private FragmentData[] fragments;
    private int length;

    private final List<RenderableEntry> renderables = new ArrayList<>();

    public CompoundRenderable(int initialCapacity) {
        fragments = new FragmentData[initialCapacity];
    }

    public CompoundRenderable() {
        this(10);
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

    public void trimToSize() {
        if(fragments.length > length) {
            FragmentData[] newArray = new FragmentData[length];
            System.arraycopy(fragments, 0, newArray, 0, length);
            fragments = newArray;
        }
    }

    @Override
    public ArraySegment<FragmentData> getFragments() {
        return new ArraySegment<>(fragments, length, 0);
    }

    public void addRenderable(Renderable renderable) {
        ArraySegment<FragmentData> newFragments = renderable.getFragments();
        int newFragmentsLength = newFragments.getLength();

        ensureCapacity(newFragmentsLength);
        newFragments.copyTo(0, fragments, length, newFragmentsLength);

        renderables.add(new RenderableEntry(renderable, length, newFragmentsLength));
        length += newFragmentsLength;
    }

    public void updateRenderable(int index) {
        if(index < 0 || index >= renderables.size()) { //fail fast for updating invalid index
            throw new IndexOutOfBoundsException("index out of bounds for renderable");
        }

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
        }
    }

    public void removeRenderable(int index) {
        if(index < 0 || index >= renderables.size()) { //fail fast for updating index
            throw new IndexOutOfBoundsException("index out of bounds for renderable");
        }

        RenderableEntry target = renderables.get(index);
        if(target.length > 0 && index < renderables.size() - 1) {
            System.arraycopy(fragments, target.fragmentIndex + target.length, fragments, target.fragmentIndex,
                    length - target.fragmentIndex - target.length);

            //shift indices back
            for(int i = index + 1; i < renderables.size(); i++) {
                renderables.get(i).fragmentIndex -= target.length;
            }
        }

        renderables.remove(index);
        length -= target.length;
    }

    public Renderable getRenderable(int index) {
        return renderables.get(index).renderable;
    }
}