package io.github.zap.arenaapi.pathfind;

import io.github.zap.nms.common.world.VoxelShapeWrapper;
import io.github.zap.vector.MutableWorldVector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultWalkNodeProvider extends NodeProvider {
    @Override
    public @NotNull PathNode[] generateNodes(@NotNull PathfinderContext context, @NotNull PathAgent agent,
                                             @NotNull PathNode at) {

        return new PathNode[0];
    }

    /**
     * Simplified, faster algorithm for entities whose width is < 1
     */
    private @Nullable PathNode tryWalkDirectionalSimplified(@NotNull PathfinderContext context, @NotNull PathAgent agent,
                                                  @NotNull PathNode node, @NotNull Direction direction) {
        PathNode candidate = node.chain(direction);
        MutableWorldVector nodePosition = candidate.position().asMutable();

        return null;
    }

    private double simpleRequiredJumpHeight(PathAgent agent, BlockCollisionProvider provider, MutableWorldVector nodePosition) {
        double jumpHeightRequired = 0;
        double headroom = 0;
        double spillover = 0; //this helps us account for blocks with collision height larger than 1

        double jumpHeight = agent.characteristics().jumpHeight;
        double height = agent.characteristics().height;

        int iterations = (int)Math.ceil(jumpHeight);
        for(int i = 0; i < iterations; i++) {
            VoxelShapeWrapper shape = provider.getBlock(nodePosition).collision();

            double minY = shape.minY();
            double maxY = shape.isEmpty() ? 1 : shape.maxY();

            if(shape.isEmpty()) {
                headroom++;
                spillover = Math.min(spillover - 1, 0);

                double additionalHeadroom = maxY - spillover;
                if(additionalHeadroom > 0) {
                    headroom += additionalHeadroom;
                }
            }
            else if(shape.isFull()) {
                headroom = 0;
                jumpHeightRequired++;

                spillover = Math.min(spillover - 1, 0);
            }
            else {
                double newSpillover = spillover - maxY;

                if(newSpillover <= 0) { //maxY larger or equal to spillover
                    spillover = maxY - 1;
                    jumpHeightRequired += Math.abs(newSpillover);

                    double headroomIncrement = minY - spillover;
                    if(headroomIncrement > 0) {
                        headroom += headroomIncrement;
                    }
                }
                else { //maxY smaller than spillover
                    spillover = newSpillover;
                    headroom = 0;
                }
            }

            if(jumpHeight >= jumpHeightRequired && height <= headroom) {
                return jumpHeightRequired;
            }

            nodePosition.add(Direction.UP);
        }

        return -1;
    }
}
