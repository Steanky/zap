package io.github.zap.zombies.game2.player.coin;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface Coins {

    default void addCoins(int amount) {
        addCoins(amount, Component.empty());
    }

    void addCoins(int amount, @NotNull Component reason);

    int getCoins();

}
