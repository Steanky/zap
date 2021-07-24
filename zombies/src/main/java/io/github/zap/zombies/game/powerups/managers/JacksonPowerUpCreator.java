package io.github.zap.zombies.game.powerups.managers;

import io.github.zap.zombies.game.powerups.PowerUp;
import io.github.zap.zombies.game.powerups.spawnrules.PowerUpSpawnRule;
import org.jetbrains.annotations.NotNull;

public class JacksonPowerUpCreator implements PowerUpCreator {

    @Override
    public @NotNull PowerUp createPowerUp(String name) {
        return null;
    }

    @Override
    public @NotNull PowerUpSpawnRule<@NotNull ?> createSpawnRule(String name, String powerUpName) {
        return null;
    }

}
