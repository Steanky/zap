package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.shop.ShopType;
import lombok.Getter;
import org.bukkit.util.Vector;

/**
 * Data for a block shop
 */
@Getter
public abstract class BlockShopData extends ShopData {
    private final Vector blockLocation;

    private final Vector hologramLocation;

    public BlockShopData(ShopType type, boolean requiresPower, Vector blockLocation, Vector hologramLocation) {
        super(type, requiresPower);
        this.blockLocation = blockLocation;
        this.hologramLocation = hologramLocation;
    }
}
