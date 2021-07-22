package io.github.zap.arenaapi.game.arena.event;

import io.github.zap.arenaapi.game.arena.player.ManagedPlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Args for a managed {@link Event} with an associated {@link ManagedPlayer}
 * @param <P> The type of managed player that caused this event
 * @param <E> The type of the event
 */
public record ManagedPlayerArgs<@NotNull P extends ManagedPlayer, @NotNull E extends Event>(@NotNull P player,
                                                                                            @NotNull E event) {

}
