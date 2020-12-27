package io.github.zap.zombies.game.data.map.shop;

import lombok.Getter;
import org.bukkit.util.Vector;

public class BlockShopData extends ShopData {

    @Getter
    private Vector blockLocation;

    @Getter
    private Vector hologramLocation;

    protected BlockShopData() {

    }

}
