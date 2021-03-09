package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.shop.ShopType;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for a lucky chest
 */
@SuppressWarnings({"FieldMayBeFinal", "unused"})
@Getter
public class LuckyChestData extends ShopData {

    private List<String> equipments = new ArrayList<>();

    private List<Pair<List<Sound>, Long>> jingle = new ArrayList<>();

    private Vector chestLocation;

    private int cost = 0;

    private long sittingTime = 0L;

    public LuckyChestData(Vector chestLocation) {
        super(ShopType.LUCKY_CHEST, false);
        this.chestLocation = chestLocation;
    }

    private LuckyChestData() {
        super(ShopType.LUCKY_CHEST, false);
    }

}
