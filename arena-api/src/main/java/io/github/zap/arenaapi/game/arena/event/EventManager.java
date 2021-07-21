package io.github.zap.arenaapi.game.arena.event;

import io.github.zap.arenaapi.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Manages {@link org.bukkit.event.Event}s within an {@link io.github.zap.arenaapi.game.arena.Arena}
 * Implementations should validate events to make sure that they are applicable to their Arena
 */
public interface EventManager {

    /**
     * Gets the proxy for {@link PlayerEvent}s
     * @param bukkitEventClass The class of the event
     * @param <E> The type of the event
     * @return The proxy event
     */
    @NotNull <E extends PlayerEvent> Event<E> getPlayerProxy(@NotNull Class<E> bukkitEventClass);

    /**
     * Gets the proxy for {@link EntityEvent}s
     * @param bukkitEventClass The class of the event
     * @param <E> The type of the event
     * @return The proxy event
     */
    @NotNull <E extends EntityEvent> Event<E> getEntityProxy(@NotNull Class<E> bukkitEventClass);

    /**
     * Gets the proxy for {@link BlockEvent}s
     * @param bukkitEventClass The class of the event
     * @param <E> The type of the event
     * @return The proxy event
     */
    @NotNull <E extends BlockEvent> Event<E> getBlockProxy(@NotNull Class<E> bukkitEventClass);

    /**
     * Gets the proxy for {@link InventoryEvent}s
     * @param bukkitEventClass The class of the event
     * @param <E> The type of the event
     * @return The proxy event
     */
    @NotNull <E extends InventoryEvent> Event<E> getInventoryProxy(@NotNull Class<E> bukkitEventClass);

}
