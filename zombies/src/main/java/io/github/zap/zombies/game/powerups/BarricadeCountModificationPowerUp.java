package io.github.zap.zombies.game.powerups;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.powerups.ModifierModificationPowerUpData;
import io.github.zap.zombies.game.data.powerups.PowerUpData;

@PowerUpType(name = "Barricade-Count-Modification")
public class BarricadeCountModificationPowerUp extends PowerUp{
    public BarricadeCountModificationPowerUp(ModifierModificationPowerUpData data, ZombiesArena arena) {
        this(data, arena, 10);
    }

    public BarricadeCountModificationPowerUp(ModifierModificationPowerUpData data, ZombiesArena arena, int refreshRate) {
        super(data, arena, refreshRate);
    }

    @Override
    public void activate() {
        // TODO: Implement
    }
}
