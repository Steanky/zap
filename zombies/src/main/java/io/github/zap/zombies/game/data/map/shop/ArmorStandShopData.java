package io.github.zap.zombies.game.data.map.shop;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

@Getter
@AllArgsConstructor
public class ArmorStandShopData extends ShopData {

    public ArmorStandShopData(String type) {
        super(type);
    }

    protected ArmorStandShopData(String type, Vector blockLocation, BlockFace blockFace) {
        super(type);
        this.blockLocation = blockLocation;
        this.blockFace = blockFace;
    }

    Vector blockLocation;

    BlockFace blockFace;

}
