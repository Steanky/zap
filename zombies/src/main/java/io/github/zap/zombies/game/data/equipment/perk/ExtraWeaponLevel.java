package io.github.zap.zombies.game.data.equipment.perk;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Level of the extra weapon perk
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
public class ExtraWeaponLevel extends PerkLevel {

    /**
     * Maps new slots to equipment object group types
     */
    private Map<Integer, String> newSlots = new HashMap<>();

}
