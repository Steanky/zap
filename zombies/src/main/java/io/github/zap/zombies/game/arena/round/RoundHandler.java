package io.github.zap.zombies.game.arena.round;

import io.github.zap.zombies.game.arena.spawner.ZombieCountChangedArgs;
import io.github.zap.zombies.game.data.map.RoundData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    /**
     * Gets the 0-based index of the round
     * @return The round index
     */
    int getCurrentRoundIndex();

    /**
     * Gets the
     * @return
     */
    @Nullable RoundData getCurrentRound();

}
