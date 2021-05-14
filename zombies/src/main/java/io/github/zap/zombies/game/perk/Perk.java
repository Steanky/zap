package io.github.zap.zombies.game.perk;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.ObjectDisposedException;
import io.github.zap.arenaapi.event.Event;
import io.github.zap.zombies.game.ZombiesPlayer;
import lombok.Getter;

/**
 * Base class for perks. Perks can be event-based: their actions are performed when the provided event is fired. If
 * perks do not require any kind of action, the event can be set to null.
 * @param <T> The type of arguments the event passes to the perk executor.
 */
public abstract class Perk<T> implements Disposable {
    @Getter
    private final ZombiesPlayer owner;

    private final Event<T> actionTriggerEvent;

    @Getter
    private final int maxLevel;

    @Getter
    private final boolean resetLevelOnDisable;

    @Getter
    private int currentLevel;

    protected boolean disposed = false;

    /**
     * Creates a new perk instance for this player.
     * @param owner The player to whom the perk applies
     * @param actionTriggerEvent The event that triggers this perk's effects (can be null)
     * @param maxLevel How many levels the perk has
     */
    public Perk(ZombiesPlayer owner, Event<T> actionTriggerEvent, int maxLevel, boolean resetLevelOnDisable) {
        this.owner = owner;
        this.actionTriggerEvent = actionTriggerEvent;
        this.maxLevel = maxLevel;
        this.resetLevelOnDisable = resetLevelOnDisable;
    }

    /**
     * Upgrades this perk (and registers the execute handler with this event, if present). Can be called multiple
     * times to increase the perk level.
     * @return True if the perk level changed as a result of this call; false otherwise
     */
    public boolean upgrade() {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        if(currentLevel < maxLevel) {
            if(++currentLevel == 1 && actionTriggerEvent != null) {
                actionTriggerEvent.registerHandler(this::execute);
            }

            activate();
            return true;
        }

        return false;
    }

    /**
     * Downgrades this perk (and unregisters the execute handler, if present).
     * @return True if the perk level changed as a result of this call, false otherwise
     */
    public boolean downgrade() {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        if(currentLevel > 0) {
            if(--currentLevel == 0) {
                if(actionTriggerEvent != null) {
                    actionTriggerEvent.removeHandler(this::execute);
                }
                disable();
                return true;
            }

            activate();
            return true;
        }

        return false;
    }

    /**
     * Applies the perk's effects. This is called every time the perk is upgraded or downgraded (if the downgrade
     * brings the perk down one level but is still not 0). It is also called when perks should be re-activated, such
     * as when the player rejoins the game.
     */
    public abstract void activate();

    /**
     * Removes the perk's effects. This is called when the player leaves the game, or when the perk's level has been
     * reduced to 0.
     */
    public abstract void deactivate();

    /**
     * Disables any effects applied by the perk. Called once by downgrade() if the downgrade brought this perk to 0, or
     * externally when the player leaves the game.
     */
    public final void disable() {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        if(resetLevelOnDisable) {
            currentLevel = 0;
        }

        deactivate();
    }

    @Override
    public void dispose() {
        if(disposed) {
            return;
        }

        disable();

        if(actionTriggerEvent != null) {
            actionTriggerEvent.dispose();
        }

        disposed = true;
    }

    /**
     * Performs the action associated with the perk. This is called automatically.
     * @param args The args passed by the event
     */
    public abstract void execute(T args);
}