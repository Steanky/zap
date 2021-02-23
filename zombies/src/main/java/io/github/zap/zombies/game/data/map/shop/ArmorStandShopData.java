package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.shop.ShopType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.util.Vector;

/**
 * Data for an armor stand shop
 */
@Getter
public abstract class ArmorStandShopData extends ShopData {
    private final Vector rootLocation; //these can be final because this class is not directly constructed by Jackson

    private final Vector hologramLocation;

    public ArmorStandShopData(ShopType type, boolean requiresPower, Vector rootLocation, Vector hologramLocation) {
        super(type, requiresPower);
        this.rootLocation = rootLocation;
        this.hologramLocation = hologramLocation;
    }
}
