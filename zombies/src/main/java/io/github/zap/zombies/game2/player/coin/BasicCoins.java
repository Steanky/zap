package io.github.zap.zombies.game2.player.coin;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class BasicCoins implements Coins {

    private int coins;

    public BasicCoins(int startingCoins) {
        this.coins = startingCoins;
    }

    @Override
    public void addCoins(int amount, @NotNull Component reason) {
        coins += amount;
    }

    @Override
    public int getCoins() {
        return coins;
    }

}
