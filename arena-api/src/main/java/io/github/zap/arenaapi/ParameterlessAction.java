package io.github.zap.arenaapi;

/**
 * Represents an action that takes no parameters and returns a value.
 * @param <T>
 */
public interface ParameterlessAction<T> {
    T invoke();
}
