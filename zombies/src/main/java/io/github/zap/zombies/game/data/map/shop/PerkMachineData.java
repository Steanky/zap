package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.perk.PerkType;
import lombok.Getter;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for a perk machine
 */
@Getter
public class PerkMachineData extends BlockShopData {

    private PerkType perkType = PerkType.DEFAULT;

    private List<Integer> costs = new ArrayList<>();

    public PerkMachineData(Vector blockLocation, Vector hologramLocation) {
        super(blockLocation, hologramLocation);
    }
}
