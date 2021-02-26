package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.shop.ShopType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Data for a shop
 */
@Getter
@AllArgsConstructor
public abstract class ShopData {
    private final ShopType type;

    private final boolean requiresPower;
}
