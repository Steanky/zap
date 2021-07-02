package io.github.zap.arenaapi.pathfind;

import io.github.zap.nms.common.world.BlockSnapshot;
import io.github.zap.nms.common.world.VoxelShapeWrapper;
import io.github.zap.vector.ImmutableWorldVector;
import io.github.zap.vector.MutableWorldVector;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

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

        ImmutableWorldVector walkingTo = node.add(direction).asImmutable();
        double offset = 0.5 - (agent.characteristics().width() / 2);
        BoundingBox agentBounds = agent.characteristics().getBounds().shift(node.add(offset, 0, offset).asBukkit());

        switch (determineType(context, agentBounds, direction, walkingTo, node)) {
            case FALL:
                ImmutableWorldVector fallVec = fallTest(context.blockProvider(), walkingTo);
                return fallVec == null ? null : node.chain(fallVec);
            case JUMP:
                MutableWorldVector jumpVec = jumpTest(agent, agentBounds, context.blockProvider(), walkingTo, direction);
                return jumpVec == null ? null : node.chain(jumpVec);
            case NO_CHANGE:
                return node.chain(walkingTo);
        }

        return null;
    }

    private JunctionType determineType(PathfinderContext context, BoundingBox agentBounds, Direction direction,
                                       ImmutableWorldVector target, PathNode current) {
        if(collidesMovingAlong(agentBounds, context.blockProvider(), direction)) {
            return JunctionType.JUMP;
        }
        else {
            BlockSnapshot belowTarget = context.blockProvider().getBlock(target.add(Direction.DOWN));
            BlockSnapshot belowAgent = context.blockProvider().getBlock(current.add(Direction.DOWN));

            if(belowTarget != null && belowAgent != null) {
                double targetY = belowTarget.collision().maxY();
                double currentY = belowAgent.collision().maxY();

                if(targetY == currentY) {
                    return JunctionType.NO_CHANGE;
                }
                else {
                    return JunctionType.FALL;
                }
            }

            return JunctionType.IGNORE;
        }
    }

    private MutableWorldVector jumpTest(PathAgent agent, BoundingBox agentBounds, BlockCollisionProvider provider,
                                        ImmutableWorldVector start, Direction direction) {
        double jumpHeightRequired = 0;
        double headroom = 0;
        double spillover = 0; //this helps us account for blocks with collision height larger than 1

        double jumpHeight = agent.characteristics().jumpHeight();
        double height = agent.characteristics().height();

        MutableWorldVector jumpVector = start.asMutable();
        MutableWorldVector seek = start.asMutable();

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
                spillover = Math.max(spillover - 1, 0);

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

    private ImmutableWorldVector fallTest(BlockCollisionProvider provider, ImmutableWorldVector seek) {
        MutableWorldVector endVector = seek.asMutable();

        while(endVector.y() >= 0) {
            BlockSnapshot snapshot = provider.getBlock(endVector);

            if(snapshot == null) {
                return null;
            }

            VoxelShapeWrapper collision = snapshot.collision();

            if(collision.isFull()) {
                return endVector.add(Direction.UP).asImmutable();
            }
            else if(!collision.isEmpty()) {
                endVector.add(0, collision.maxY(), 0);
                break;
            }

            endVector.add(Direction.DOWN);
        }

        return null;
    }

    private boolean collidesMovingAlong(BoundingBox agentBounds, BlockCollisionProvider provider, Direction direction) {
        BoundingBox expanded = agentBounds.clone().expandDirectional(direction.asBukkit());

        if(!direction.isIntercardinal()) {
            return provider.collidesWithAnySolid(expanded);
        }

        List<BlockSnapshot> candidates = provider.collidingSolids(expanded);

        if(candidates.size() > 0) {
            double halfWidth = agentBounds.getWidthX() / 2;
            double negativeHalfWidth = -halfWidth;
            int dirFactor = direction.blockX() * direction.blockZ();

            return processCollisions(candidates, dirFactor < 0 ? collision -> fastDiagonalCollisionCheck(halfWidth,
                    negativeHalfWidth, dirFactor, collision.getMinX(), collision.getMinZ(), collision.getMaxX(),
                    collision.getMaxZ()) : collision -> fastDiagonalCollisionCheck(halfWidth, negativeHalfWidth, dirFactor,
                    collision.getMaxX(), collision.getMinZ(), collision.getMinX(), collision.getMaxZ()));
        }

        return false;
    }

    private void calculateAversion(PathNode node, BlockCollisionProvider provider) {
        BlockSnapshot standingOn = provider.getBlock(node.add(Direction.DOWN));

        if(standingOn != null) {
            double materialAversion = getAversionCalculator().aversionForMaterial(standingOn.data().getMaterial());
            double factor = getAversionCalculator().aversionFactor(node);
            node.score.setG(node.parent.score.getG() + materialAversion + (factor * node.distance(node.parent)));
        }
    }

    /**
     * fast enough to make our lord and savior Josh approve
     * this is my favorite method in the entire plugin
     */
    private boolean fastDiagonalCollisionCheck(double halfWidth, double negativeHalfWidth, int dirFac, double minX, double minZ,
                                               double maxX, double maxZ) {
        double zMinusXMin = minZ - (minX * dirFac);
        if(!(zMinusXMin <= halfWidth)) {
            return maxZ - (maxX * dirFac) <= halfWidth;
        }

        if(zMinusXMin >= negativeHalfWidth) {
            return true;
        }

        return maxZ - (maxX * dirFac) >= negativeHalfWidth;
    }

    private boolean processCollisions(List<BlockSnapshot> candidates, Function<BoundingBox, Boolean> collides) {
        for(BlockSnapshot snapshot : candidates) {
            for(BoundingBox collision : snapshot.collision().boundingBoxes()) {
                collision.shift(snapshot.position().blockX(), snapshot.position().blockY(), snapshot.position().blockZ());

                if(collides.apply(collision)) {
                    return true;
                }
            }
        }

        return false;
    }
}