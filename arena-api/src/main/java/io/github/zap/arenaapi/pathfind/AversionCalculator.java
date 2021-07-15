package io.github.zap.arenaapi.pathfind;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Calculates the "aversion" mobs have towards certain blocks.
 */
public interface AversionCalculator {
    AversionCalculator DEFAULT_WALK = new AversionCalculator() {
        @Override
        public double aversionForMaterial(@NotNull Material material) {
            return switch (material) {
                case SOUL_SAND, SLIME_BLOCK, HONEY_BLOCK -> 4;
                case FIRE -> 8;
                case COBWEB -> 16;
                case CACTUS, SWEET_BERRY_BUSH -> 12;
                case LAVA -> 256;
                case WATER -> 3;
                default -> 0;
            };
        }

        @Override
        public double aversionFactor(@NotNull PathNode linkedNode) {
            return linkedNode.parent.y() - linkedNode.y() <= 3 ? 1 : 2;
        }
    };

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
