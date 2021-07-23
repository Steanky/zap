package io.github.zap.zombies.game.arena.round;

import io.github.zap.zombies.game.arena.spawner.ZombieCountChangedArgs;
import org.jetbrains.annotations.NotNull;

/**
 * Manages remaining zombies and plays through rounds
 */
public interface RoundHandler {

    /**
     * Called when the game first begins
     */
    void onGameBegin();

    /**
     * Called when the number of zombies remaining changes
     * @param args The args for the event
     */
    void onZombieCountChanged(@NotNull ZombieCountChangedArgs args);

}
