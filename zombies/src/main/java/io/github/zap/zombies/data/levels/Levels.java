package io.github.zap.zombies.data.levels;

import java.util.Map;

/**
 * Holds a set of levels that can be accessed by index
 * @param <T> The type of the level to store
 */
public interface Levels<T> {

    /**
     * Gets an indexed level
     * @param level The index of the level to get
     * @return The level
     */
    Map<String, T> getLevel(int level);

    /**
     * Gets the number of levels
     * @return The number of levels
     */
    int size();

}
