package io.github.zap.zombies.game.powerups.spawnrules;

import io.github.zap.arenaapi.event.Event;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.powerups.PowerUp;
import io.github.zap.zombies.game.powerups.events.ChangedAction;
import io.github.zap.zombies.game.powerups.events.PowerUpChangedEventArgs;
import lombok.Getter;
import org.bukkit.Location;

import java.util.Collections;

public abstract class PowerUpSpawnRule<T extends SpawnRuleData> {
    @Getter
    private final T data;

    @Getter
    private final ZombiesArena arena;

    @Getter
    private final Event<PowerUp> powerUpSpawned = new Event<>();

    @Getter
    private final String spawnTargetName;

    public PowerUpSpawnRule(String spawnTargetName, T data, ZombiesArena arena) {
        this.data = data;
        this.arena = arena;
        this.spawnTargetName = spawnTargetName;
    }

    protected void spawn(Location loc) {
        var pu = getArena().getPowerUpManager().createPowerUp(getSpawnTargetName(), getArena());
        pu.spawnItem(loc);
        powerUpSpawned.callEvent(pu);
        var eventArgs = new PowerUpChangedEventArgs(ChangedAction.ADD, Collections.singleton(pu));
        getArena().getPowerUps().add(pu);
        getArena().getPowerUpChangedEvent().callEvent(eventArgs);
    }
}
