package io.github.zap.arenaapi.pathfind.calculate;

import io.github.zap.arenaapi.pathfind.path.PathNode;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Calculates the "aversion" mobs have towards certain blocks.
 */
public interface AversionCalculator {
    /**
     * Calculates any "aversion" that mobs may have towards travelling across or through certain blocks. In general,
     * this is used to make mobs smarter (they can avoid damaging themselves on harmful blocks, travelling over blocks
     * that slow them down, etc. This value is additive — returning 0 will simply add nothing to the path distance.
     * @param material The material the mob is going to contact
     * @return A double value indicating the number of additional blocks moving this way should cost. Return 0 for no
     * bias. To bias mobs away from travelling to this block, increase the value. To completely stop mobs from travelling
     * here, return Double.POSITIVE_INFINITY. To bias mobs towards travelling here, return a negative number.
     */
    double aversionForMaterial(@NotNull Material material);

    /**
     * Calculates the distance for the given PathNode. This is additive with the result returned from aversionFor.
     * @param linkedNode The node to calculate the distance for
     * @return The distance, which will be added to the result returned by aversionFor
     */
    double aversionFactor(@NotNull PathNode linkedNode);
}
