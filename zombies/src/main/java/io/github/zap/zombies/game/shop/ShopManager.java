package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.game.arena.event.ManagedPlayerArgs;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Manages all {@link Shop}s within a {@link io.github.zap.zombies.game.ZombiesArena}
 */
public interface ShopManager extends Disposable {

    /**
     * Attempts to purchase an item at a {@link Shop}.
     * @param args The args to purchase with
     * @return Whether any shops interacted with the purchase attempt
     */
    boolean checkForPurchases(@NotNull ManagedPlayerArgs<@NotNull ZombiesPlayer, ? extends @NotNull PlayerEvent> args);

}
