package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.perk.PerkType;
import lombok.Getter;

import java.util.List;

/**
 * Data for a perk machine
 */
@Getter
public class PerkMachineData extends BlockShopData {

    private PerkType perkType;

    private List<Integer> costs;

    private PerkMachineData() {

    }

}
