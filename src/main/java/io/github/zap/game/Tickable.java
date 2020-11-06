package io.github.zap.game;

/**
 * Classes that wish to periodically run code on the main server thread must implement this.
 */
public interface Tickable extends Named {
    /**
     * Executes the game tick code on the main server thread.
     */
    void onTick();
}
