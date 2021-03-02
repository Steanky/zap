package io.github.zap.zombies.game.powerups;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;

@Getter
@Setter
public class DurationPowerUpData extends PowerUpData {
    int duration = 600; // In ticks

    BarColor bossBarColor = BarColor.WHITE;
}
