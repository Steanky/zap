package io.github.zap.zombies.game.data.map.shop;

import lombok.Getter;

@Getter
public class GunShopData extends ArmorStandShopData {

    private String gunName;

    private int cost;

    private int refillCost;

    private GunShopData() {

    }


}
