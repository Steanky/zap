package io.github.zap.zombies.game.data.powerups;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModifierModificationPowerUpData extends PowerUpData {
    double multiplier;
    double amount;
}
