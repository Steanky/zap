package io.github.zap.game;

public interface Tickable {
    /**
     * Gets the unique name of this Tickable instance.
     * @return The name of this Tickable instance
     */
    String getName();

    /**
     * Executes the game tick code on the main server thread.
     */
    void doTick();
}
