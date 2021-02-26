package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.shop.ShopType;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for an armor shop
 */
public class ArmorShopData extends ArmorStandShopData {
    @Getter
    private final List<ArmorLevel> armorLevels = new ArrayList<>();

    private ArmorShopData() {
        super(ShopType.ARMOR_SHOP, false, null, null);
    }

    public ArmorShopData(Vector rootLocation, Vector hologramLocation) {
        super(ShopType.ARMOR_SHOP, false, rootLocation, hologramLocation);
    }

    /**
     * A level of an armor shop's available armor levels
     */
    @Getter
    public static class ArmorLevel {
        private final String name = "default";

        private final int cost = 0;

        private final Material[] materials = new Material[0];

        private ArmorLevel() { }
    }

}
