package io.github.zap.zombies.game.player.task;

import io.github.zap.zombies.game.ZombiesArena;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Player task that can be influenced by a bukkit event
 * @param <E> The influencing bukkit event
 */
public abstract class EventToggledPlayerTask<E extends Event> extends PlayerTask {

    private final Class<E> clazz;

    public EventToggledPlayerTask(@NotNull ZombiesArena arena, long delay, long period, @NotNull Class<E> clazz) {
        super(arena, delay, period);
        this.clazz = clazz;
    }

    /**
     * Event handler for the toggling event
     * @param event The toggling event
     */
    public abstract void onEvent(@NotNull E event);

    /**
     * Checks if this player task is toggled by an event class
     * @param otherClass The tested event class
     * @return Whether it is toggled by the class
     */
    public boolean acceptsEventClass(Class<? extends Event> otherClass) {
        return clazz.equals(otherClass);
    }

}
