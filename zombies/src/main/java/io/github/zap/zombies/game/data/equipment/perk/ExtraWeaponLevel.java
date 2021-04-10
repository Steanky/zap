package io.github.zap.zombies.game.data.equipment.perk;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * Level of the extra weapon perk
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
public class ExtraWeaponLevel extends PerkLevel {

    private Set<Integer> newSlots = new HashSet<>();

}
