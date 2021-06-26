package io.github.zap.arenaapi.pathfind;

import io.github.zap.nms.common.world.VoxelShapeWrapper;
import io.github.zap.vector.MutableWorldVector;
import io.github.zap.vector.VectorAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultWalkNodeProvider extends NodeProvider {
    @Override
    public @NotNull PathNode[] generateNodes(@NotNull PathfinderContext context, @NotNull PathAgent agent,
                                             @NotNull PathNode at) {
        PathNode[] nodes = new PathNode[8];
        int j = 0;
        for(int i = 0; i < 8; i++) {
            PathNode node = tryWalkDirectionalSimplified(context, agent, at, Direction.valueAtIndex(i));

            if(node != null) {
                nodes[j++] = node;
            }
        }

        return nodes;
    }

    /**
     * Simplified, faster algorithm for entities whose width is < 1
     */
    private @Nullable PathNode tryWalkDirectionalSimplified(@NotNull PathfinderContext context, @NotNull PathAgent agent,
                                                  @NotNull PathNode node, @NotNull Direction direction) {
        if(agent.characteristics().width > 1) {
            throw new UnsupportedOperationException("You cannot use this NodeProvider for thick entities (yet!)");
        }

        PathNode candidate = node.chain(direction);
        MutableWorldVector nodePosition = candidate.position().asMutable();
        VectorAccess access = simpleRequiredJumpHeight(agent, context.blockProvider(), nodePosition);

        PathNode newNode = null;
        if(access != null) {
            newNode = node.chain(access);
        }

        return newNode;
    }

    private @Nullable VectorAccess simpleRequiredJumpHeight(PathAgent agent, BlockCollisionProvider provider, MutableWorldVector nodePosition) {
        VectorAccess lastSolid = null;

        double jumpHeightRequired = 0;
        double headroom = 0;
        double spillover = 0; //this helps us account for blocks with collision height larger than 1

        double jumpHeight = agent.characteristics().jumpHeight;
        double height = agent.characteristics().height;

        int iterations = (int)Math.ceil(jumpHeight);
        boolean lastWasFull = false;
        for(int i = 0; i < iterations; i++) {
            VoxelShapeWrapper shape = provider.getBlock(nodePosition).collision();

            double minY = shape.minY();
            double maxY = shape.isEmpty() ? 1 : shape.maxY();

            if(shape.isEmpty()) {
                spillover = Math.min(spillover - 1, 0);

                double additionalHeadroom = maxY - spillover;
                if(additionalHeadroom > 0) {
                    headroom += additionalHeadroom;
                }

                if(lastWasFull) {
                    lastWasFull = false;
                    lastSolid = nodePosition.copyVector();
                }
            }
            else if(shape.isFull()) {
                headroom = 0;
                jumpHeightRequired++;

                spillover = Math.min(spillover - 1, 0);
                lastWasFull = true;
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

                if(lastWasFull) {
                    lastWasFull = false;
                    lastSolid = nodePosition.copyVector();
                }
            }

            if(jumpHeight >= jumpHeightRequired && height <= headroom) {
                return lastSolid;
            }

            nodePosition.add(Direction.UP);
        }

        return lastSolid;
    }
}
