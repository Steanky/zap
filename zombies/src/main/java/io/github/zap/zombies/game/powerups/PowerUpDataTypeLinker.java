package io.github.zap.zombies.game.powerups;

public interface PowerUpDataTypeLinker {
    String getName();
    Class<? extends PowerUpData> getDataType();

}
