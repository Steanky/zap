package io.github.zap.zombies.game.powerups;

import lombok.Value;

public abstract class SpawnRulesData {
    public abstract PowerUpSpawnRule createSpawnRule();
}
