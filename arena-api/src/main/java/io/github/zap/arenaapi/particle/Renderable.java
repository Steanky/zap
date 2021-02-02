package io.github.zap.arenaapi.particle;

import java.util.Iterator;

/**
 * Interface for an object that provides an iterable collection of renderable fragment data.
 */
public interface Renderable {
    /**
     * Gets the iterator used to iterate over the fragments.
     * @return The fragment iterator
     */
    FragmentData[] getFragments();
}
