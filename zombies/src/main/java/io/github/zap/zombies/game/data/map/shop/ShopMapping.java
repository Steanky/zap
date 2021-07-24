package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.shop.Shop;
import org.jetbrains.annotations.NotNull;

/**
 * Mapping to create {@link Shop}s from its data
 * @param <D> The type of the shop's data
 */
@FunctionalInterface
public interface ShopMapping<D extends @NotNull ShopData> {

    /**
     * Creates a shop from its data
     * @param shopData The shop's data
     * @return The new shop
     */
    @NotNull Shop<D> createShop(@NotNull D shopData);

}
