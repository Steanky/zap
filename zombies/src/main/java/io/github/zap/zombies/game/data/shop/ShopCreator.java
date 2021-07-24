package io.github.zap.zombies.game.data.shop;

import io.github.zap.zombies.game.shop.Shop;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Creates {@link Shop}s from its {@link ShopData}
 */
public interface ShopCreator {

    @Nullable <D extends @NotNull ShopData> Shop<D> createShop(@NotNull D shopData);

}
