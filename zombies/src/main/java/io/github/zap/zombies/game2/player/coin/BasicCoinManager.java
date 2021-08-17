package io.github.zap.zombies.game2.player.coin;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class BasicCoinManager implements CoinManager {

    private int coins;

    public BasicCoinManager(int startingCoins) {
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
