package io.github.zap.arenaapi;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a unique object.
 */
@FunctionalInterface
public interface Unique {

    /**
     * Gets the ID of this object, which should be unique and unchanging for its lifetime.
     * @return The ID of this object
     */
    @NotNull UUID getId();

}
