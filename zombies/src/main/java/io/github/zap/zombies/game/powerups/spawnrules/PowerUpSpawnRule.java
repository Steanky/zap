package io.github.zap.zombies.game.powerups.spawnrules;

import io.github.zap.arenaapi.event.Event;
import io.github.zap.zombies.game.arena.round.RoundHandler;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.powerups.spawnrules.SpawnRuleData;
import io.github.zap.zombies.game.powerups.PowerUp;
import io.github.zap.zombies.game.powerups.events.ChangedAction;
import io.github.zap.zombies.game.powerups.events.PowerUpChangedEventArgs;
import io.github.zap.zombies.game.powerups.managers.PowerUpCreator;
import lombok.Getter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * This class or its subclasses create Power ups when a condition is met
 * @param <T>
 */
public abstract class PowerUpSpawnRule<T extends @NotNull SpawnRuleData> {

    private final @NotNull MapData map;

    private final @NotNull PowerUpCreator powerUpCreator;

    private final @NotNull RoundHandler roundHandler;

    @Getter
    private final T data;

    @Getter
    private final Event<PowerUp> powerUpSpawned = new Event<>();

    @Getter
    private final String spawnTargetName;

    public PowerUpSpawnRule(@NotNull MapData map, @NotNull PowerUpCreator powerUpCreator,
                            @NotNull RoundHandler roundHandler, T data, @NotNull String spawnTargetName) {
        this.map = map;
        this.powerUpCreator = powerUpCreator;
        this.roundHandler = roundHandler;
        this.data = data;
        this.spawnTargetName = spawnTargetName;
    }

    /**
     * Determines whether the current round restrict power up (eg: Boss rounds)
     * @return Whether this round disables power up
     */
    public boolean isDisabledRound() {
        var disabledRounds = map.getDisablePowerUpRounds();
        var currentRound = roundHandler.getCurrentRoundIndex();
        return disabledRounds.contains(currentRound);
    }

    /**
     * Spawn the targeted power up
     * @param loc the location to spawn
     */
    protected void spawn(@NotNull Location loc) {
        var pu = powerUpCreator.createPowerUp(getSpawnTargetName());
        pu.spawnItem(loc);
        powerUpSpawned.callEvent(pu);
        var eventArgs = new PowerUpChangedEventArgs(ChangedAction.ADD, Collections.singleton(pu));
        getArena().getPowerUps().add(pu);
        getArena().getPowerUpChangedEvent().callEvent(eventArgs);
    }
}
