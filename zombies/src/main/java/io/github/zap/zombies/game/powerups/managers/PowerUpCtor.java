package io.github.zap.zombies.game.powerups.managers;

import io.github.zap.zombies.game.data.powerups.PowerUpData;
import io.github.zap.zombies.game.powerups.PowerUp;
import org.jetbrains.annotations.NotNull;

/**
 * Creates {@link PowerUp}s from their data
 */
@FunctionalInterface
public interface PowerUpCtor<T extends PowerUp> {

    T construct(@NotNull String name, @NotNull PowerUpData data);

}
