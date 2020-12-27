package io.github.zap.zombies.game.data.map.shop;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class LuckyChestData extends ArmorStandShopData {

    private List<String> equipments = new ArrayList<>();

    private int cost;

    private int rollsUntilMove;

    private long sittingTime;

    private LuckyChestData() {

    }

}
