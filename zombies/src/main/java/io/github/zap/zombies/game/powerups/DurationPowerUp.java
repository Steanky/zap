package io.github.zap.zombies.game.powerups;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

@PowerUpType(getName = "Duration")
public class DurationPowerUp extends PowerUp {
    @Getter
    private long estimatedEndTimeStamp;

    private BukkitTask timeoutTask;

    public DurationPowerUp(DurationPowerUpData data, ZombiesArena arena) {
        super(data, arena);
    }

    public DurationPowerUp(DurationPowerUpData data, ZombiesArena arena, int refreshRate) {
        super(data, arena, refreshRate);
    }

    @Override
    public void activate() {
        restartTimeoutTimer();
    }

    protected void restartTimeoutTimer() {
        if(getState() != PowerUpState.ACTIVATED)
            throw new IllegalStateException("The perk must be activated to call this method!");

        stopTimeoutTimer();
        var duration = ((DurationPowerUpData)getData()).getDuration();

        timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                if(getState() == PowerUpState.ACTIVATED)
                    deactivate();
            }
        }.runTaskLater(Zombies.getInstance(), duration);

        estimatedEndTimeStamp = System.currentTimeMillis() + duration * 50;
    }

    protected void stopTimeoutTimer() {
        if(timeoutTask != null && !timeoutTask.isCancelled()) {
            timeoutTask.cancel();
        }
    }
}
