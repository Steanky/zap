package io.github.zap.game.data;

import io.github.zap.serialize.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ShopData extends DataSerializable {
    private ShopType type;

    private ShopData() {}
}
