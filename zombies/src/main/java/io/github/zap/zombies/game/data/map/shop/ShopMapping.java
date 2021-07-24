package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.shop.Shop;
import org.jetbrains.annotations.NotNull;

/**
 * Mapping to create {@link Shop}s from its data
 */
@FunctionalInterface
public interface ShopMapping {

    /**
     * Creates a shop from its data
     * @param shopData The shop's data
     * @param <D> The type of the shop's data
     * @return The new shop
     */
    @NotNull <D extends @NotNull ShopData> Shop<D> createShop(@NotNull D shopData);

}
