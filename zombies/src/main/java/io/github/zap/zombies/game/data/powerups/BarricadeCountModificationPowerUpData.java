package io.github.zap.zombies.game.data.powerups;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BarricadeCountModificationPowerUpData extends ModifierModificationPowerUpData {
    boolean affectAll; // For performance: when set to true ignore affectedRange
    double affectedRange;
    int rewardGold;
}
