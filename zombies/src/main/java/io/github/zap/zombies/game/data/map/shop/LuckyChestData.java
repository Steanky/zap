package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.shop.ShopType;
import io.github.zap.zombies.game.util.Jingle;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for a lucky chest
 */
@Getter
public class LuckyChestData extends ShopData {

    private final List<String> equipments = new ArrayList<>();

    private final List<ImmutablePair<List<Jingle.Note>, Long>> jingle = new ArrayList<>();

    private final Vector chestLocation;

    private final int cost = 0;

    private final int rollsUntilMove = 0;

    private final long sittingTime = 0L;

    public LuckyChestData(Vector chestLocation) {
        super(ShopType.LUCKY_CHEST, false);
        this.chestLocation = chestLocation;
    }
}
