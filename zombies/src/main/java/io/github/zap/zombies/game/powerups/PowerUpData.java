package io.github.zap.zombies.game.powerups;

import lombok.Value;
import org.bukkit.Material;

@Value
public class PowerUpData {
    String type;
    String name;
    String powerUpType;

    String displayName;
    Material itemRepresentation;
}
