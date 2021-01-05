package io.github.zap.zombies.game.data.map.shop;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

/**
 * Data for an armor stand shop
 */
@Getter
@AllArgsConstructor
public class ArmorStandShopData extends ShopData {

    Vector blockLocation;

    BlockFace blockFace;

    protected ArmorStandShopData() {

    }

}
