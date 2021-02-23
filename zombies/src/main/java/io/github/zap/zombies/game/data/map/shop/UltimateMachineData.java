package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.shop.ShopType;
import lombok.Getter;
import org.bukkit.util.Vector;

/**
 * Data for an ultimate machine
 */
@Getter
public class UltimateMachineData extends BlockShopData {
    private int cost = 0;

    public UltimateMachineData(Vector blockLocation, Vector hologramLocation) {
        super(ShopType.ULTIMATE_MACHINE, true, blockLocation, hologramLocation);
    }
}
