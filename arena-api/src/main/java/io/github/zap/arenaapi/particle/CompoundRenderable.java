package io.github.zap.arenaapi.particle;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public abstract class CompoundRenderable implements Renderable {
    @AllArgsConstructor
    private static class RenderableEntry {
        private final Renderable renderable;
        private int fragmentIndex;
    }

    private FragmentData[] fragments;

    private final List<RenderableEntry> renderables = new ArrayList<>();

    @Override
    public FragmentData[] getFragments() {
        return fragments;
    }

    private int renderableLength(int renderableIndex) {
        int renderablesSize = renderables.size();
        int nextRenderable = renderableIndex + 1;
        return (nextRenderable >= renderablesSize ? renderablesSize : renderables.get(nextRenderable).fragmentIndex) -
                renderables.get(renderableIndex).fragmentIndex;
    }

    public void addRenderable(Renderable renderable) {
        if(renderable == this) { //prevent infinite recursion lol
            throw new IllegalArgumentException("cannot add this object as a renderable");
        }

        renderables.add(new RenderableEntry(renderable, fragments.length));

        FragmentData[] renderableFragments = renderable.getFragments();
        FragmentData[] newFrags = new FragmentData[fragments.length + renderableFragments.length];

        System.arraycopy(fragments, 0, newFrags, 0, fragments.length);
        System.arraycopy(renderableFragments, 0, newFrags, fragments.length, renderableFragments.length);

        fragments = newFrags;
    }

    public void updateRenderable(int index) {
        if(index < 0 || index >= renderables.size()) { //fail fast for updating index
            throw new IndexOutOfBoundsException("index out of bounds for renderable");
        }

        RenderableEntry targetEntry = renderables.get(index);
        FragmentData[] renderableFrags = targetEntry.renderable.getFragments();

        int oldLength = renderableLength(index);
        int diff = renderableFrags.length - oldLength;

        if(diff == 0) { //size didn't change, use single arraycopy
            System.arraycopy(renderableFrags, 0, fragments, targetEntry.fragmentIndex, renderableFrags.length);
        }
        else {
            FragmentData[] newFrags = new FragmentData[fragments.length + diff];

            System.arraycopy(fragments, 0, newFrags, 0, targetEntry.fragmentIndex);
            System.arraycopy(renderableFrags, 0, newFrags, targetEntry.fragmentIndex, newFrags.length);
            System.arraycopy(fragments, targetEntry.fragmentIndex + oldLength, newFrags,
                    targetEntry.fragmentIndex + newFrags.length, fragments.length - oldLength - targetEntry.fragmentIndex);

            fragments = newFrags;
        }
    }

    public void removeRenderable(int index) {
        if(index < 0 || index >= renderables.size()) { //fail fast for updating index
            throw new IndexOutOfBoundsException("index out of bounds for renderable");
        }

        int targetLength = renderableLength(index);
        int nextIndex = renderables.get(index).fragmentIndex + targetLength;
        FragmentData[] newFrags = new FragmentData[fragments.length - targetLength];

        System.arraycopy(fragments, 0, newFrags, 0, index);

        if(nextIndex < fragments.length) {
            System.arraycopy(fragments, nextIndex, newFrags, index, fragments.length - index - newFrags.length);
        }

        for(int i = index + 1; i < renderables.size(); i++) {
            renderables.get(i).fragmentIndex -= targetLength;
        }

        renderables.remove(index);

        fragments = newFrags;
    }

    public Renderable getRenderable(int index) {
        return renderables.get(index).renderable;
    }
}
