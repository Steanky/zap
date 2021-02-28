package io.github.zap.zombies.game.powerups;

import io.github.zap.zombies.game.ZombiesArena;
import lombok.Value;
import org.bukkit.Material;

@Value
public class PowerUpData {
    String type;
    String name;
    SpawnRulesData spawnRulesData;
    String powerUpType;

    String displayName;
    Material itemRepresentation;
}
