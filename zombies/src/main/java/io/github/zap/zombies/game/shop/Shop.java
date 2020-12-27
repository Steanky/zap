package io.github.zap.zombies.game.shop;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.ShopData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public abstract class Shop<D extends ShopData> {

    @Getter
    private final ZombiesArena zombiesArena;

    @Getter
    private final D shopData;


    public abstract void displayTo(Player player);

    public abstract boolean purchase(ZombiesPlayer zombiesPlayer);

    public abstract boolean shouldInteractWith(Object object);

}
