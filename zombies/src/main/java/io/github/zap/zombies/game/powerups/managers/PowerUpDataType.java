package io.github.zap.zombies.game.powerups.managers;

import io.github.zap.zombies.game.data.powerups.EarnedGoldMultiplierPowerUpData;
import io.github.zap.zombies.game.data.powerups.ModifierModificationPowerUpData;
import io.github.zap.zombies.game.data.powerups.PowerUpData;
import lombok.Getter;

public enum PowerUpDataType implements PowerUpDataTypeLinker {
    BASIC("basic", PowerUpData.class),
    EARNED_GOLD_MOD("earned_gold_mod", EarnedGoldMultiplierPowerUpData.class),
    MULTIPLIER("multiplier", ModifierModificationPowerUpData.class);

    @Getter
    private final String name;

    @Getter
    private final Class<? extends PowerUpData> dataType;

    PowerUpDataType(String name, Class<? extends PowerUpData> dataType) {
        this.name = name;
        this.dataType = dataType;
    }
}
