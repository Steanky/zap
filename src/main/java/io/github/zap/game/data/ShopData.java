package io.github.zap.game.data;

import io.github.zap.serialize.DataSerializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * Defines a shop. Not sure what to put here - perhaps hardcoding shops isn't a bad idea given the limited degree
 * to which we can abstract them?
 */
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopData extends DataSerializable {
    ShopType type;

    private ShopData() {}
}
