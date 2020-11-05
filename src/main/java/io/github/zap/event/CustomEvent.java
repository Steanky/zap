package io.github.zap.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for custom events. This implements the most simple functions required by Bukkit.
 */
public class CustomEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * This function is required by Bukkit's event API.
     * @return The list of handlers
     */
    @NotNull
    public static HandlerList getHandlerList() {  //req'd by bukkit event api
        return handlers;
    }
}
