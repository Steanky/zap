package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.shop.ShopType;
import io.github.zap.zombies.game.util.Jingle;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for a lucky chest
 */
@Getter
public class LuckyChestData extends ShopData {

    private List<String> equipments = new ArrayList<>();

    private List<Pair<List<Jingle.Note>, Long>> jingle = new ArrayList<>();

    private Vector chestLocation;

    private int cost = 0;

    private int rollsUntilMove = 0;

    private long sittingTime = 0L;

    public LuckyChestData(Vector chestLocation) {
        super(ShopType.LUCKY_CHEST, false);
        this.chestLocation = chestLocation;
    }

    private LuckyChestData() {
        super(ShopType.LUCKY_CHEST, false);

    }
}
