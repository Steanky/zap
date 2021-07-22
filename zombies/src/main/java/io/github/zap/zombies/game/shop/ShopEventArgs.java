package io.github.zap.zombies.game.shop;

import io.github.zap.zombies.game.player.ZombiesPlayer;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

/**
 * Event args for when a shop item is purchased by a {@link ZombiesPlayer}
 */
public record ShopEventArgs<P extends @NotNull ZombiesPlayer>(@NotNull Shop<@NotNull ?> shop, @NotNull P player) {

}
