package io.github.zap.zombies.game2.powerup;

import io.github.zap.zombies.game2.powerup.visual.PowerUpVisual;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class BasicPowerUp implements PowerUp {

    private final PowerUpVisual visual;

    public BasicPowerUp(PowerUpVisual visual) {
        this.visual = visual;
    }

    @Override
    public void activate() {

    }

    @Override
    public void deactivate() {

    }

    @Override
    public void tick() {

    }
}
