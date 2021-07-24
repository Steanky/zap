package io.github.zap.zombies.game.shop;

import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Event args for when a shop item is purchased by a {@link ZombiesPlayer}
 */
public record ShopEventArgs<S extends @NotNull Shop<@NotNull ?>, P extends @NotNull ZombiesPlayer>(@NotNull S shop, @NotNull P player) {

}
