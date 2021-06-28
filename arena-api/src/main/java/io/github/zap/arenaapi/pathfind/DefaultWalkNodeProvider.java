package io.github.zap.arenaapi.pathfind;

import io.github.zap.nms.common.world.VoxelShapeWrapper;
import io.github.zap.vector.MutableWorldVector;
import io.github.zap.vector.VectorAccess;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultWalkNodeProvider extends NodeProvider {
    @Override
    public @NotNull PathNode[] generateNodes(@NotNull PathfinderContext context, @NotNull PathAgent agent,
                                             @NotNull PathNode at) {
        PathNode[] nodes = new PathNode[8];

        int j = 0;
        for(int i = 0; i < 8; i++) {
            PathNode node = walkDirectional(context, agent, at, Direction.valueAtIndex(i));

            if(node != null) {
                nodes[j++] = node;
            }
        }

        return nodes;
    }

    /**
     * Simplified, faster algorithm for entities whose width is < 1
     */
    private @Nullable PathNode walkDirectional(@NotNull PathfinderContext context, @NotNull PathAgent agent,
                                               @NotNull PathNode node, @NotNull Direction direction) {
        if(agent.characteristics().width() > 1) {
            throw new UnsupportedOperationException("You cannot use this NodeProvider for thick entities (yet!)");
        }

        VectorAccess jump = heightTest(agent, context.blockProvider(), node.add(direction).asMutable());
        if(jump == null) {
            return null;
        }

        BoundingBox scaled = agent.characteristics().getBounds().expandDirectional(direction.multiply(-1).asBukkit());

        if(!context.blockProvider().collidesWithAnySolid(scaled)) {
            BoundingBox extendedBounds = new BoundingBox(0, 0, 0, agent.characteristics().width(),
                    jump.y() - agent.position().y(), agent.characteristics().width());

            if(context.blockProvider().collidesWithAnySolid(extendedBounds)) {
                return node.chain(jump);
            }
        }

        return null;
    }

    private @Nullable MutableWorldVector heightTest(PathAgent agent, BlockCollisionProvider provider, MutableWorldVector seek) {
        double jumpHeightRequired = 0;
        double headroom = 0;
        double spillover = 0; //this helps us account for blocks with collision height larger than 1

        double jumpHeight = agent.characteristics().jumpHeight();
        double height = agent.characteristics().height();

        MutableWorldVector jump = seek.copyVector();

        int iterations = (int)Math.ceil(jumpHeight + height);
        for(int i = 0; i < iterations; i++) {
            VoxelShapeWrapper shape = provider.getBlock(seek).collision();

            double minY = shape.minY();
            double maxY = shape.isEmpty() ? 1 : shape.maxY();

            if(shape.isEmpty()) { //anything w/o collision
                spillover = Math.min(spillover - 1, 0);

                double additionalHeadroom = maxY - spillover;
                if(additionalHeadroom > 0) {
                    headroom += additionalHeadroom;
                }
            }
            else if(shape.isFull()) { //full block (1x1x1)
                double newSpillover = spillover - 1;

                if(newSpillover < 0) {
                    jumpHeightRequired += Math.abs(newSpillover);
                    spillover = 0;
                }
                else {
                    spillover = newSpillover;
                }

                headroom = 0;
            }
            else { //anything that does not fall into the above categories
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

            if(jumpHeight >= jumpHeightRequired && height <= headroom) { //entity can make the jump
                return jump.add(0, jumpHeightRequired, 0);
            }

            seek.add(Direction.UP);
        }

        return null;
    }
}
