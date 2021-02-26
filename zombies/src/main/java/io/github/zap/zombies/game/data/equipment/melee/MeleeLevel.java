package io.github.zap.zombies.game.data.equipment.melee;

import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.enchantments.Enchantment;

import java.util.List;

/**
 * Level of a melee weapon
 */
public class MeleeLevel {

    @Getter
    private List<Pair<Enchantment, Integer>> enchantments;

    private MeleeLevel() {

    }
}
