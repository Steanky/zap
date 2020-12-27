package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.shop.Shop;

public interface ShopManager {

    <D extends ShopData> void addShop(String shopType, Class<D> dataClass, ShopCreator.ShopMapping<D> shopMapping);

    <D extends ShopData> Shop<D> createShop(ZombiesArena zombiesArena, D shopData);

}
