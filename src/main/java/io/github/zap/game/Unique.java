package io.github.zap.game;

/**
 * Represents some kind of uniquely named object.
 */
public interface Unique {
    /**
     * Gets the unique name of this object. No specific format is required, nor should one be assumed.
     * @return The unique name of this object
     */
    String getName();
}
