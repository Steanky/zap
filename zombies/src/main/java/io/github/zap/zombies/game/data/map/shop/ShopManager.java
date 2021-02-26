package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.shop.Shop;
import io.github.zap.zombies.game.shop.ShopType;

/**
 * Stores and manages information about shops
 */
public interface ShopManager {

    /**
     * Adds a shop mapping
     * @param shopType The string representation of the shop type
     * @param dataClass The class of the data the shop uses
     * @param shopMapping A mapping class used to create the shop from the data instance
     * @param <D> The type of the shop's data
     */
    <D extends ShopData> void addShop(ShopType shopType, Class<D> dataClass, ShopCreator.ShopMapping<D> shopMapping);

    /**
     * Creates a shop from its data
     * @param zombiesArena The arena to create the shop for
     * @param shopData The shop's data
     * @param <D> The type of the shop's data
     * @return The new shop
     */
    <D extends ShopData> Shop<D> createShop(ZombiesArena zombiesArena, D shopData);

}
