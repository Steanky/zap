package io.github.zap.zombies.game.data.map.shop;

import lombok.Getter;
import org.bukkit.util.Vector;

/**
 * Data for a gun shop
 */
@Getter
public class GunShopData extends ArmorStandShopData {

    private String gunName = "NONE";

    private int cost = 0;

    private int refillCost = 0;

    public GunShopData(Vector rootLocation, Vector hologramLocation) {
        super(rootLocation, hologramLocation);
    }

}
