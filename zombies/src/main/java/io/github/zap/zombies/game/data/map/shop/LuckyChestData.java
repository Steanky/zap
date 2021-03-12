package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.shop.ShopType;
import io.github.zap.zombies.game.util.Jingle;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Data for a lucky chest
 */
@Getter
@Setter
public class LuckyChestData extends ShopData {

    private Vector chestLocation;

    private List<Jingle.Note> jingle;

    private List<String> equipments;

    private long sittingTime = 200;

    private int cost = 1000;

    public LuckyChestData(Vector chestLocation, boolean requiresPower) {
        super(ShopType.LUCKY_CHEST, requiresPower);
        this.chestLocation = chestLocation;
    }

    public LuckyChestData() {
        super(ShopType.LUCKY_CHEST, false);
    }

    public LuckyChestData(Vector chestLocation) {
        super(ShopType.LUCKY_CHEST, false);
        this.chestLocation = chestLocation;
    }

}
