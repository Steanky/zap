package io.github.zap.zombies.game.powerups;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.bukkit.Material;

public class PowerUpData {
    @Getter
    String type;
    @Getter
    String name;
    @Getter
    String powerUpType;

    @Getter
    String displayName;
    @Getter
    Material itemRepresentation;

    // In ticks
    @Getter
    int despawnDuration = 6000;
    @Getter
    double pickupRange = 1;

}
