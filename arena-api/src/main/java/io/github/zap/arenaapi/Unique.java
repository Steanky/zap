package io.github.zap.arenaapi;

/**
 * Represents a unique object.
 */
public interface Unique {
    /**
     * Gets the ID of this object, which should be unique and unchanging for its lifetime.
     * @return The ID of this object
     */
    long getId();
}
