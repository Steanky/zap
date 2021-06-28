package io.github.zap.arenaapi.pathfind;

import io.github.zap.nms.common.world.BlockCollisionSnapshot;
import io.github.zap.nms.common.world.VoxelShapeWrapper;
import io.github.zap.vector.MutableWorldVector;
import io.github.zap.vector.VectorAccess;
import lombok.Getter;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultWalkNodeProvider extends NodeProvider {
    private enum JunctionType {
        FALL,
        JUMP,
        NO_CHANGE
    }

    @Getter
    private static final DefaultWalkNodeProvider instance = new DefaultWalkNodeProvider();

    private DefaultWalkNodeProvider() {}

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

    private JunctionType determineType(@NotNull PathfinderContext context, @NotNull PathAgent agent,
                                       @NotNull PathNode node, @NotNull Direction direction) {
        MutableWorldVector forwardVector = node.add(direction).asMutable();

        BoundingBox agentBounds = agent.characteristics().getBounds();
        agentBounds.expandDirectional(direction.asBukkit());

        if(context.blockProvider().collidesWithAnySolid(agentBounds)) {
            return JunctionType.JUMP;
        }
        else {
            forwardVector.add(Direction.DOWN);

            BlockCollisionSnapshot snapshot = context.blockProvider().getBlock(forwardVector);
            if(snapshot.collision().isFull()) {
                return JunctionType.NO_CHANGE;
            }
            else {
                return JunctionType.FALL;
            }
        }
    }

    /**
     * Simplified, faster algorithm for entities whose width is < 1
     */
    private @Nullable PathNode walkDirectional(@NotNull PathfinderContext context, @NotNull PathAgent agent,
                                               @NotNull PathNode node, @NotNull Direction direction) {
        if(agent.characteristics().width() > 1) {
            throw new UnsupportedOperationException("You cannot use this NodeProvider for thick entities (yet!)");
        }

        MutableWorldVector targetVector = node.add(direction).asMutable();

        switch (determineType(context, agent, node, direction)) {
            case FALL:
                break;
            case JUMP:
                MutableWorldVector jumpVec = jumpTest(agent, context.blockProvider(), targetVector);
                return jumpVec == null ? null : node.chain(jumpVec);
            case NO_CHANGE:
                return node.chain(direction);
        }

        return null;
    }

    private @Nullable MutableWorldVector jumpTest(PathAgent agent, BlockCollisionProvider provider, MutableWorldVector seek) {
        double jumpHeightRequired = 0;
        double headroom = 0;
        double spillover = 0; //this helps us account for blocks with collision height larger than 1

        double jumpHeight = agent.characteristics().jumpHeight();
        double height = agent.characteristics().height();

        MutableWorldVector jump = seek.copyVector();

        //TODO: make sure height can't overflow max height (255 for vanilla 1.16.5)
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
                //check if mob will collide with something on its way up
                BoundingBox agentBounds = agent.characteristics().getBounds();
                agentBounds.expandDirectional(0, jumpHeightRequired, 0);

                if(provider.collidesWithAnySolid(agentBounds)) {
                    return null;
                }

                return jump.add(0, jumpHeightRequired, 0);
            }

            seek.add(Direction.UP);
        }

        return null;
    }
}