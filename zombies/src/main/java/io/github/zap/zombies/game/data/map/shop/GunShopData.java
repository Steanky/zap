package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.shop.ShopType;
import lombok.Getter;
import org.bukkit.util.Vector;

/**
 * Data for a gun shop
 */
@Getter
public class GunShopData extends ArmorStandShopData {
    private String gunName = "NONE";

    private String gunDisplayName = "NONE";

    private int cost = 0;

    private int refillCost = 0;

    private GunShopData() {
        super(ShopType.GUN_SHOP, false, null, null);
    }

    public GunShopData(Vector rootLocation, Vector hologramLocation) {
        super(ShopType.GUN_SHOP, false, rootLocation, hologramLocation);
    }
}
