package io.github.zap.arenaapi.util;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Validate;

import java.util.List;

public final class ListUtils {
    public static <T> T randomElement(List<T> from) {
        Validate.notNull(from, "from cannot be null");

        int size = from.size();
        if(size == 0) {
            return null;
        }

        return from.get(RandomUtils.nextInt(0, size - 1));
    }
}
