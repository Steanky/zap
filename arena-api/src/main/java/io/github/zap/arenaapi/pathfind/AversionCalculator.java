package io.github.zap.arenaapi.pathfind;

import io.github.zap.nms.common.world.BlockSnapshot;

import java.util.List;

/**
 * Calculates the "aversion" mobs have towards certain blocks.
 */
public interface AversionCalculator {
    /**
     * Calculates any "aversion" that mobs may have towards travelling across or through certain blocks. In general,
     * this is used to make mobs smarter (they can avoid damaging themselves on harmful blocks, travelling over blocks
     * that slow them down, etc.
     * @param startingAt The block the entity is above
     * @param movingTo The block the entity will be above, if it moves
     * @param collidesWith A list of BlockSnapshots the entity will collide with if it moves
     * @return A double value indicating the number of additional blocks moving this way should cost. Return 0 for no
     * bias. To bias mobs away from travelling to this block, increase the value. To completely stop mobs from travelling
     * here, return Double.POSITIVE_INFINITY. To bias mobs towards travelling here, return a negative number.
     */
    double additionalDistance(BlockSnapshot startingAt, BlockSnapshot movingTo, List<BlockSnapshot> collidesWith);
}
