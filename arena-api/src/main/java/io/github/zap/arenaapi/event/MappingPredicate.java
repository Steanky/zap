package io.github.zap.arenaapi.event;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Represents a function that performs both validation and a mapping conversion in a single call.
 * @param <T> The input type
 * @param <U> The output type
 */
public interface MappingPredicate<T, U> {
    /**
     * Maps the provided input, validating it first. If the input is invalid, the first parameter of the returned
     * pair will be false and the second will be null. If the input is valid, it will return true and the value will be
     * non-null.
     * @param input The input value
     */
    Pair<Boolean, U> tryMap(T input);
}
