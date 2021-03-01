package io.github.zap.zombies.game.powerups;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class DurationPowerUp extends PowerUp {
    public DurationPowerUp(DurationPowerUpData data, ZombiesArena arena) {
        super(data, arena);
    }

    public DurationPowerUp(DurationPowerUpData data, ZombiesArena arena, int refreshRate) {
        super(data, arena, refreshRate);
    }

    private BukkitTask timeoutTask;

    @Override
    public void activate() {
        restartTimeoutTimer();
    }

    protected void restartTimeoutTimer() {
        if(getState() != PowerUpState.ACTIVATED)
            throw new IllegalStateException("The perk must be activated to call this method!");

        if(timeoutTask != null && !timeoutTask.isCancelled()) {
            timeoutTask.cancel();
        }

        timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                deactivate();
            }
        }.runTaskLater(Zombies.getInstance(), ((DurationPowerUpData)getData()).getTimeoutDuration());
    }
}
