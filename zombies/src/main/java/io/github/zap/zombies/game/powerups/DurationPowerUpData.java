package io.github.zap.zombies.game.powerups;

import lombok.Getter;
import org.bukkit.Material;

public class DurationPowerUpData extends PowerUpData {
    @Getter
    int timeoutDuration = 6000;
}
