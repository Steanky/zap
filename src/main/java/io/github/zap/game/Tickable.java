package io.github.zap.game;

public interface Tickable extends Named {
    /**
     * Executes the game tick code on the main server thread.
     */
    void doTick();
}
