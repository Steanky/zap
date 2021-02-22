package io.github.zap.zombies.game.data.map.shop;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.util.Vector;

/**
 * Data for a block shop
 */
@Getter
@AllArgsConstructor
public class BlockShopData extends ShopData {

    private Vector blockLocation;

    private Vector hologramLocation;

}
