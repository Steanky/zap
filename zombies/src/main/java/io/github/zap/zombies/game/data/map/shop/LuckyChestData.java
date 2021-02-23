package io.github.zap.zombies.game.data.map.shop;

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

    private List<String> equipments = new ArrayList<>();

    private List<ImmutablePair<List<Jingle.Note>, Long>> jingle;

    private Vector chestLocation;

    private int cost;

    private int rollsUntilMove;

    private long sittingTime;

    private LuckyChestData() {

    }

}
