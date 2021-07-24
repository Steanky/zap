package io.github.zap.zombies.game.data.map.shop;

import org.jetbrains.annotations.NotNull;

/**
 * A pair of an {@link ShopData} and a {@link ShopMapping} that creates an associated {@link io.github.zap.zombies.game.shop.Shop}
 * @param <D> The type of the equipment data
 * @param <M> The type of the mapping
 */
public record ShopDataMappingPair<D extends @NotNull ShopData, M extends @NotNull ShopMapping<D>>(D data, M mapping) {

}
