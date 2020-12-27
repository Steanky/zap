package io.github.zap.zombies.game.data.map.shop;

import lombok.Getter;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

@Getter
public class GunShopData extends ArmorStandShopData {


    private String gunName;

    private int cost;

    private int refillCost;

    public GunShopData(Vector blockLocation, BlockFace blockFace) {
        super("gun_shop", blockLocation, blockFace);
    }

    public GunShopData() {
        super("gun_shop");
    }


}
