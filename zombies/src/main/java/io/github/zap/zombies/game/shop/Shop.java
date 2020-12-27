package io.github.zap.zombies.game.shop;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.ShopData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
@Getter
public abstract class Shop<D extends ShopData> {

    private final ZombiesArena zombiesArena;

    private final D shopData;

    @Getter
    private boolean powered = false;

    public void display() {
        for (Player player : zombiesArena.getWorld().getPlayers()) {
            displayTo(player);
        }
    }

    public void power() {
        this.powered = true;
    }

    public abstract void displayTo(Player player);

    public abstract Boolean purchase(ZombiesPlayer zombiesPlayer);

    public abstract boolean shouldInteractWith(Object object);

}
