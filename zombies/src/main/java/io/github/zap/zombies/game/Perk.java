package io.github.zap.zombies.game;

import io.github.zap.arenaapi.event.Event;
import lombok.Getter;

/**
 * Base class for perks. Perks can be event-based: their actions are performed when the provided event is fired. If
 * perks do not require any kind of action, the event can be set to null.
 * @param <T> The type of arguments the event passes to the perk executor.
 */
public abstract class Perk<T> {
    @Getter
    private final ZombiesPlayer owner;

    private final Event<T> actionTriggerEvent;

    @Getter
    private final int maxLevel;

    @Getter
    private int currentLevel;

    private boolean activateOnRejoin = false;

    /**
     * Creates a new perk instance for this player.
     * @param owner The player to whom the perk applies
     * @param actionTriggerEvent The event that triggers this perk's effects (can be null)
     * @param maxLevel How many levels the perk has
     */
    public Perk(ZombiesPlayer owner, Event<T> actionTriggerEvent, int maxLevel) {
        this.owner = owner;
        this.actionTriggerEvent = actionTriggerEvent;
        this.maxLevel = maxLevel;

        owner.getPlayerQuitEvent().registerHandler(this::onPlayerQuit);
        owner.getPlayerRejoinEvent().registerHandler(this::onPlayerRejoin);
    }

    /**
     * Ensure perks are properly deactivated if the player leaves the arena.
     * @param caller The event
     * @param args The player who quit
     */
    private void onPlayerQuit(Event<ZombiesPlayer> caller, ZombiesPlayer args) {
        if(currentLevel != 0) {
            deactivate();

            if(!args.getArena().getMap().isPerksLostOnQuit()) { //perk restoration is optional
                activateOnRejoin = true; //perks are not lost on quitting
            }
        }
    }

    private void onPlayerRejoin(Event<ZombiesPlayer> caller, ZombiesPlayer args) {
        if(activateOnRejoin) {
            activate();
            activateOnRejoin = false;
        }
    }

    /**
     * Activates this perk (and registers the execute handler with this event, if present).
     */
    public void activate() {
        if(currentLevel < maxLevel) {
            if(++currentLevel == 1) {
                actionTriggerEvent.registerHandler(this::execute);
            }
        }
    }

    /**
     * Deactivates this perk (and unregisters the execute handler, if present).
     */
    public void deactivate() {
        if(currentLevel > 0) {
            if(--currentLevel == 0) {
                actionTriggerEvent.removeHandler(this::execute);
            }
        }
    }

    /**
     * Performs cleanup tasks (deactivates and closes associated event).
     */
    public void close() {
        deactivate();

        if(actionTriggerEvent != null) {
            actionTriggerEvent.close();
        }
    }

    /**
     * Performs the action associated with the perk event.
     * @param event The event that called this handler
     * @param args The args passed by the event
     */
    public abstract void execute(Event<T> event, T args);
}
