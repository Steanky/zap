package io.github.zap.arenaapi.game.arena.event;

import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Args for a managed {@link EntityEvent} with a {@link org.bukkit.entity.Entity} that is part of an {@link io.github.zap.arenaapi.game.arena.Arena}
 * @param <T> The type of the entity
 * @param <E> The type of the event
 */
public record EntityArgs<@NotNull T, @NotNull E extends EntityEvent>(@NotNull T entity, @NotNull E event) {

}
