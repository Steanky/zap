package io.github.zap.zombies.game.data.map.shop;

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

    public ArmorShopData(Vector rootLocation, Vector hologramLocation) {
        super(rootLocation, hologramLocation);
    }

    /**
     * A level of an armor shop's available armor levels
     */
    @Getter
    public static class ArmorLevel {

        private String name;

        private int cost;

        private Material[] materials;

        private ArmorLevel() {

        }
    }

}
