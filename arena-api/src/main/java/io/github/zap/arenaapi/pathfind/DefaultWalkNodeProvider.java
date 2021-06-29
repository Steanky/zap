package io.github.zap.arenaapi.pathfind;

import io.github.zap.nms.common.world.BlockSnapshot;
import io.github.zap.nms.common.world.VoxelShapeWrapper;
import io.github.zap.vector.MutableWorldVector;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DefaultWalkNodeProvider extends NodeProvider {
    private enum JunctionType {
        FALL,
        JUMP,
        NO_CHANGE,
        IGNORE
    }

    public DefaultWalkNodeProvider(@NotNull AversionCalculator aversionCalculator) {
        super(aversionCalculator);
    }

    @Override
    public void generateNodes(@Nullable PathNode[] buffer, @NotNull PathfinderContext context, @NotNull PathAgent agent,
                              @NotNull PathNode current) {
        int j = 0;
        for(int i = 0; i < 8; i++) {
            PathNode node = walkDirectional(context, agent, current, Direction.valueAtIndex(i));

            if(node != null) {
                calculateAversion(node, context.blockProvider());
                buffer[j++] = node;
            }
        }

        if(j < 8) {
            buffer[j] = null;
        }
    }

    private PathNode walkDirectional(PathfinderContext context, PathAgent agent, PathNode node, Direction direction) {
        if(agent.characteristics().width() > 1) {
            throw new UnsupportedOperationException("You cannot use this NodeProvider for thick entities (yet!)");
        }

        MutableWorldVector targetVector = node.add(direction).asMutable();
        double halfWidth = agent.characteristics().width() / 2;
        BoundingBox agentBounds = agent.characteristics().getBounds().shift(node
                .add(0.5 - halfWidth, 0, 0.5 - halfWidth).asBukkit());

        switch (determineType(context, agentBounds, node, direction)) {
            case FALL:
                MutableWorldVector fallVec = fallTest(context.blockProvider(), targetVector);
                return fallVec == null ? null : node.chain(fallVec);
            case JUMP:
                MutableWorldVector jumpVec = jumpTest(agent, agentBounds, context.blockProvider(), targetVector, direction);
                return jumpVec == null ? null : node.chain(jumpVec);
            case NO_CHANGE:
                return node.chain(direction);
        }

        return null;
    }

    private JunctionType determineType(PathfinderContext context, BoundingBox agentBounds, PathNode node, Direction direction) {
        MutableWorldVector forwardVector = node.add(direction).asMutable();

        if(collidesMovingAlong(agentBounds, context.blockProvider(), direction)) {
            return JunctionType.JUMP;
        }
        else {
            forwardVector.add(Direction.DOWN);
            BlockSnapshot snapshot = context.blockProvider().getBlock(forwardVector);

            if(snapshot != null) {
                if(snapshot.collision().isFull()) {
                    return JunctionType.NO_CHANGE;
                }
                else if(snapshot.collision().isEmpty()) {
                    return JunctionType.FALL;
                }
                else {
                    return JunctionType.NO_CHANGE;
                }
            }

            return JunctionType.IGNORE;
        }
    }

    private MutableWorldVector jumpTest(PathAgent agent, BoundingBox agentBounds, BlockCollisionProvider provider,
                                        MutableWorldVector seek, Direction direction) {
        double jumpHeightRequired = 0;
        double headroom = 0;
        double spillover = 0; //this helps us account for blocks with collision height larger than 1

        double jumpHeight = agent.characteristics().jumpHeight();
        double height = agent.characteristics().height();

        MutableWorldVector jumpVector = seek.copyVector();

        int iterations = (int)Math.ceil(jumpHeight + height);
        for(int i = 0; i < iterations; i++) {
            BlockSnapshot snapshot = provider.getBlock(seek);
            if(snapshot == null) {
                return null;
            }

            VoxelShapeWrapper shape = snapshot.collision();

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
                BoundingBox verticalTest = agentBounds.clone().expandDirectional(0, jumpHeightRequired, 0);

                if(provider.collidesWithAnySolid(verticalTest)) { //check if mob will collide with something on its way up
                    return null;
                }

                BoundingBox jumpedAgent = agentBounds.clone().shift(0, jumpHeightRequired, 0);
                if(!collidesMovingAlong(jumpedAgent, provider, direction)) {
                    return jumpVector.add(0, jumpHeightRequired, 0);
                }
                else {
                    return null;
                }
            }

            seek.add(Direction.UP);

            if(seek.y() >= 256) {
                return null;
            }
        }

        return null;
    }

    private MutableWorldVector fallTest(BlockCollisionProvider provider, MutableWorldVector seek) {
        MutableWorldVector endVector = seek.copyVector();
        while(true) {
            seek.add(Direction.DOWN);

            if(seek.y() < 0) {
                return null;
            }

            BlockSnapshot snapshot = provider.getBlock(seek);

            if(snapshot == null) {
                return null;
            }

            VoxelShapeWrapper collision = snapshot.collision();

            if(collision.isFull()) {
                break;
            }
            else if(collision.isEmpty()) {
                endVector.add(0, -1, 0);
            }
            else {
                endVector.add(0, -(1 - collision.maxY()), 0);
                break;
            }
        }

        return endVector;
    }

    private boolean collidesMovingAlong(BoundingBox bounds, BlockCollisionProvider provider, Direction direction) {
        BoundingBox expanded = bounds.clone().expandDirectional(direction.asBukkit());
        if(!direction.isIntercardinal()) {
            return provider.collidesWithAnySolid(expanded);
        }

        List<BlockSnapshot> candidates = provider.collidingSolids(expanded);

        if(candidates.size() > 0) {
            for(BlockSnapshot snapshot : candidates) {
                for(BoundingBox collision : snapshot.collision().boundingBoxes()) {
                    collision.shift(snapshot.position().asBukkit());

                    double Ax = bounds.getCenterZ();
                    double Bx = collision.getCenterZ();

                    double Az = bounds.getCenterZ();
                    double Bz = collision.getCenterZ();

                    //magic equation, DM steank for exhaustive proof
                    double delta = (Math.abs(Az - Bz) - Math.abs(Ax - Bx)) / 2;

                    if(bounds.clone().shift(Ax > Bx ? delta : -delta, 0, Az > Bz ? delta : -delta)
                            .overlaps(collision)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void calculateAversion(PathNode node, BlockCollisionProvider provider) {
        BlockSnapshot block = provider.getBlock(node.add(Direction.DOWN));

        if(block != null) {
            double materialAversion = getAversionCalculator().aversionForMaterial(block.data().getMaterial());
            double distanceAversion = getAversionCalculator().aversionForNode(node);
            double sum = materialAversion + distanceAversion;

            node.score.setG(Double.isFinite(node.score.getG()) ? node.score.getG() + sum : sum);
        }
    }
}