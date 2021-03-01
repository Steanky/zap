package io.github.zap.zombies.game.powerups.managers;


import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.powerups.spawnrules.PowerUpSpawnRule;
import io.github.zap.zombies.game.powerups.spawnrules.SpawnRuleData;

import java.lang.reflect.InvocationTargetException;

@FunctionalInterface
public interface SpawnRuleCtor<D extends SpawnRuleData, T extends PowerUpSpawnRule<D>> {
    T construct(String name, SpawnRuleData data, ZombiesArena arena);
}
