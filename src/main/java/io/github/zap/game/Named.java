package io.github.zap.game;

/**
 * Represents some kind of uniquely named object. Used to identify Tickables, but other interfaces that need this
 * functionality should extend from this.
 */
public interface Named {
    /**
     * Gets the unique name of this object.
     * @return The unique name of this object.
     */
    String getName();
}
