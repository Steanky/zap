package io.github.zap.zombies.game.data.map.shop;

import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

public class ArmorShopData extends ArmorStandShopData {

    @Getter
    private List<ArmorLevel> armorLevels;

    @Getter
    public static class ArmorLevel {

        private String name;

        private int cost;

        private Material[] materials;

        private ArmorLevel() {

        }
    }

    private ArmorShopData() {

    }

}
