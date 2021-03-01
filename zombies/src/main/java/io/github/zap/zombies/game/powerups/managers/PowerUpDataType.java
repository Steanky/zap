package io.github.zap.zombies.game.powerups.managers;

import io.github.zap.zombies.game.powerups.PowerUpData;
import io.github.zap.zombies.game.powerups.managers.PowerUpDataTypeLinker;
import io.github.zap.zombies.game.powerups.spawnrules.DefaultPowerUpSpawnRuleData;
import lombok.Getter;

public enum PowerUpDataType implements PowerUpDataTypeLinker {
    BASIC("basic", PowerUpData.class);

    @Getter
    private final String name;

    @Getter
    private final Class<? extends PowerUpData> dataType;

    PowerUpDataType(String name, Class<? extends PowerUpData> dataType) {
        this.name = name;
        this.dataType = dataType;
    }
}
