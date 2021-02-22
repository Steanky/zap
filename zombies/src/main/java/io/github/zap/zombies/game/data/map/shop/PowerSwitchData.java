package io.github.zap.zombies.game.data.map.shop;

import lombok.Getter;
import org.bukkit.util.Vector;

/**
 * Data for a power switch
 */
@Getter
public class PowerSwitchData extends BlockShopData {

    private int cost = 0;

    public PowerSwitchData(Vector blockLocation, Vector hologramLocation) {
        super(blockLocation, hologramLocation);
    }
}
