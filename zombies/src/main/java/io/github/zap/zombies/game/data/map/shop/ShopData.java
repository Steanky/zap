package io.github.zap.zombies.game.data.map.shop;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * Data for a shop
 */
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopData {

    String type;

    boolean requiresPower;

    protected ShopData() {}
}
