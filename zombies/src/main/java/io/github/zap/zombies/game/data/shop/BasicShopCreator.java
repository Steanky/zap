package io.github.zap.zombies.game.data.shop;

import io.github.zap.zombies.game.shop.Shop;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic implementation of a {@link ShopCreator}
 */
public class BasicShopCreator implements ShopCreator {

    private final @NotNull Map<@NotNull Class<? extends ShopData>, @NotNull ShopMapping<@NotNull ?>> shopMappings = new HashMap<>();

    public BasicShopCreator(@NotNull List<@NotNull ShopDataMappingPair<@NotNull ?, @NotNull ?>> shopMappings) {
        for (@NotNull ShopDataMappingPair<@NotNull ?, @NotNull ?> shopDataMappingPair : shopMappings) {
            this.shopMappings.put(shopDataMappingPair.clazz(), shopDataMappingPair.mapping());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <D extends @NotNull ShopData> Shop<D> createShop(@NotNull D shopData) {
        ShopMapping<@NotNull ?> mapping = shopMappings.get(shopData.getClass());
        return (mapping != null) ? ((ShopMapping<D>) mapping).createShop(shopData) : null;
    }

}
