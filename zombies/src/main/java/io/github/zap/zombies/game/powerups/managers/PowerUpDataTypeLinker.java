package io.github.zap.zombies.game.powerups.managers;

import io.github.zap.zombies.game.data.powerups.PowerUpData;

public interface PowerUpDataTypeLinker {
    String getName();
    Class<? extends PowerUpData> getDataType();

}
