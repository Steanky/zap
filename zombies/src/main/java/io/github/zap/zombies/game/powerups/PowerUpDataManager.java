package io.github.zap.zombies.game.powerups;

import io.github.zap.zombies.game.ZombiesArena;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Set;
import java.util.function.BiFunction;

public interface PowerUpDataManager {
    Set<PowerUpData> getDataSet();
    void addPowerUpData(PowerUpData data);
    void removePowerUpsData(String name);

    Set<ImmutablePair<BiFunction<ZombiesArena, PowerUpData, PowerUp>, Class<? extends PowerUpData>>> getPowerUpInitializers();
    void registerPowerUp(String name, BiFunction<ZombiesArena, PowerUpData, PowerUp> powerUpsInitializer, Class<? extends PowerUpData> dataClass);
    void registerPowerUp(String name, Class<? extends PowerUp> classType);
    void registerPowerUp(Class<? extends PowerUp> classType);
    void unregisterPowerUp(String name);

    PowerUp createPowerUp(String name, ZombiesArena arena);
}
