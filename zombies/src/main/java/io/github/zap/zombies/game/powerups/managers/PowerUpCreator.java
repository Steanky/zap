package io.github.zap.zombies.game.powerups.managers;

import io.github.zap.zombies.game.powerups.PowerUp;
import io.github.zap.zombies.game.powerups.spawnrules.PowerUpSpawnRule;
import org.jetbrains.annotations.NotNull;

/**
 * Creates {@link PowerUp}s and {@link PowerUpSpawnRule}s from their data
 */
public interface PowerUpCreator {

    /**
     * Create a new power up instance
     * @param name the name of the power up
     * @return a new power up, or null if no matching power up could be created
     */
    @NotNull PowerUp createPowerUp(String name);

    /**
     * Create a new power up spawnrule instance
     * @param name the name of the power up spawn rule
     * @param powerUpName the power up name that this spawnrule will spawn
     * @return a new power up spawnrule, or null if no matching power up spawn rule could be created
     */
    @NotNull PowerUpSpawnRule<@NotNull ?> createSpawnRule(String name, String powerUpName);

}
