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

    @Getter
    private boolean active = false;

    private final Event<T> actionTriggerEvent;

    private boolean activateOnRejoin = false;

    public Perk(ZombiesPlayer owner, Event<T> actionTriggerEvent) {
        this.owner = owner;
        this.actionTriggerEvent = actionTriggerEvent;

        owner.getPlayerQuitEvent().registerHandler(this::onPlayerQuit);
        owner.getPlayerRejoinEvent().registerHandler(this::onPlayerRejoin);
    }

    /**
     * Ensure perks are properly deactivated if the player leaves the arena.
     * @param caller The event
     * @param args The player who quit
     */
    private void onPlayerQuit(Event<ZombiesPlayer> caller, ZombiesPlayer args) {
        if(active) {
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
        if(!active) {
            if(actionTriggerEvent != null) {
                actionTriggerEvent.registerHandler(this::execute);
            }

            active = true;
        }
    }

    /**
     * Deactivates this perk (and unregisters the execute handler, if present).
     */
    public void deactivate() {
        if(active) {
            if(actionTriggerEvent != null) {
                actionTriggerEvent.removeHandler(this::execute);
            }

            active = false;
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
