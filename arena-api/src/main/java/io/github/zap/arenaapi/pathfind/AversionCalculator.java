package io.github.zap.arenaapi.pathfind;

import org.bukkit.Material;

/**
 * Calculates the "aversion" mobs have towards certain blocks.
 */
public interface AversionCalculator {
    AversionCalculator DEFAULT_WALK = new AversionCalculator() {
        @Override
        public double aversionForMaterial(Material material) {
            switch (material) {
                case SOUL_SAND:
                case SLIME_BLOCK:
                case HONEY_BLOCK:
                    return 2;
                case CAVE_AIR:
                case VOID_AIR:
                    return 0.00069420; //for the memes
                case FIRE:
                    return 16;
                case COBWEB:
                    return 32;
                case CACTUS:
                case SWEET_BERRY_BUSH:
                    return 8;
                case LAVA:
                    return 256;
                case WATER:
                    return 3;
                default:
                    return 0;
            }
        }

        @Override
        public double aversionForNode(PathNode linkedNode) {
            if(linkedNode.parent == null) {
                return 0;
            }

            double height = linkedNode.parent.y() - linkedNode.y();

            if(height <= 4) {
                return height;
            }
            else {
                return height / 2;
            }
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
    double aversionForMaterial(Material material);

    /**
     * Calculates the distance for the given PathNode. This is additive with the result returned from aversionFor.
     * @param linkedNode The node to calculate the distance for
     * @return The distance, which will be added to the result returned by aversionFor
     */
    double aversionForNode(PathNode linkedNode);
}
