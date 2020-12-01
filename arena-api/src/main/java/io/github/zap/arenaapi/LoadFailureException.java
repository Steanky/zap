package io.github.zap.arenaapi;

/**
 * Exception thrown when an object required for a plugin was unable to load correct due to some exceptional condition.
 */
public class LoadFailureException extends Exception {
    public LoadFailureException(String message) {
        super(message);
    }
}