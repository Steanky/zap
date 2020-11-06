package io.github.zap.game;

/**
 * Represents some kind of uniquely named object. Used to identify Tickables and objects that are accessing values from
 * a MultiAccessor.
 */
public interface Named {
    /**
     * Gets the unique name of this object.
     * @return The unique name of this object.
     */
    String getName();
}
