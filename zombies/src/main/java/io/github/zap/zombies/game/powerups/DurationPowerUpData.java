package io.github.zap.zombies.game.powerups;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;

public class DurationPowerUpData extends PowerUpData {
    @Getter
    int duration = 6000;

    @Getter
    BarColor bossBarColor;
}
