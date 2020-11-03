package io.github.zap.game.data;

import io.github.zap.serialize.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ShopData extends DataSerializable {
    @Getter
    private ShopType type;

    private ShopData() {}
}
