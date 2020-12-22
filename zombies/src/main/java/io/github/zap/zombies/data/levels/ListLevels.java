package io.github.zap.zombies.data.levels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implements levels with a list
 * @param <T> The type of the level to store
 */
public class ListLevels<T> implements Levels<T> {

    private final List<Map<String, T>> levels = new ArrayList<>();

    @Override
    public Map<String, T> getLevel(int level) {
        return levels.get(level);
    }

    @Override
    public int size() {
        return levels.size();
    }
}
