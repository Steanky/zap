package io.github.zap.zombies.game.powerups.managers;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.SpawnRule;
import io.github.zap.zombies.game.powerups.PowerUp;
import io.github.zap.zombies.game.powerups.PowerUpData;
import io.github.zap.zombies.game.powerups.spawnrules.PowerUpSpawnRule;
import io.github.zap.zombies.game.powerups.spawnrules.SpawnRuleData;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Set;
import java.util.function.BiFunction;

// order of add/register: Power-up type -> Power-up / spawn rule type -> spawn rule
public interface PowerUpManager {
    Set<PowerUpData> getDataSet();
    void addPowerUpData(PowerUpData data);
    void removePowerUpsData(String name);

    Set<SpawnRuleData> getSpawnRules();
    void addSpawnRuleData(SpawnRuleData spawnRuleData);
    void removeSpawnRuleData(String name);

    Set<ImmutablePair<BiFunction<PowerUpData,ZombiesArena, PowerUp>, Class<? extends PowerUpData>>> getPowerUpInitializers();
    void registerPowerUp(String name, BiFunction<PowerUpData,ZombiesArena, PowerUp> powerUpsInitializer, Class<? extends PowerUpData> dataClass);
    void registerPowerUp(String name, Class<? extends PowerUp> classType);
    void registerPowerUp(Class<? extends PowerUp> classType);
    void unregisterPowerUp(String name);

    Set<ImmutablePair<SpawnRuleCtor<?, ?>, Class<? extends SpawnRuleData>>> getSpawnRuleInitializers();
    void registerSpawnRule(String name, SpawnRuleCtor<?, ?> initializer, Class<? extends SpawnRuleData> dataClass);
    void registerSpawnRule(String name, Class<? extends PowerUpSpawnRule<?>> spawnRule);
    void registerSpawnRule(Class<? extends PowerUpSpawnRule<?>> spawnRule);

    PowerUp createPowerUp(String name, ZombiesArena arena);
    PowerUpSpawnRule<?> createSpawnRule(String name, String powerUpName, ZombiesArena arena);
}
