package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.shop.Shop;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class ShopCreator {

    @Getter
    private final Map<String, ShopMapping<?>> shopMappings = new HashMap<>();

    public interface ShopMapping<D extends ShopData> {

        Shop<D> createShop(ZombiesArena zombiesArena, D shopData);

    }

}
