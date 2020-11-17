package io.github.zap.arenaapi;

import io.github.zap.arenaapi.util.ReflectionUtils;

/**
 * Exception thrown when an object required for a plugin was unable to load correct due to some exceptional condition.
 */
public class LoadFailureException extends Exception {
    public LoadFailureException(String message) {
        super(String.format("%s caused plugin load failure: %s", ReflectionUtils.getCallerName(), message));
    }
}