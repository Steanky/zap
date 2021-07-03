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
        INCREASE,
        NO_CHANGE,
        IGNORE
    }

    private PathfinderContext context;
    private PathAgent agent;

    private double blockOffset;

    private double width;
    private double negativeWidth;

    public DefaultWalkNodeProvider(@NotNull AversionCalculator aversionCalculator) {
        super(aversionCalculator);
    }

    @Override
    public void init(@NotNull PathfinderContext context, @NotNull PathAgent agent) {
        this.context = context;
        this.agent = agent;

        blockOffset = 0.5 - (agent.characteristics().width() / 2);

        width = agent.characteristics().width();
        negativeWidth = -width;
    }

    @Override
    public void generateNodes(@Nullable PathNode[] buffer, @NotNull PathNode current) {
        int j = 0;
        for(int i = 0; i < 8; i++) {
            PathNode node = walkDirectional(context, current, Direction.valueAtIndex(i));

            if(node != null) {
                calculateAversion(node, context.blockProvider());
                buffer[j++] = node;
            }
        }

        if(j < 8) {
            buffer[j] = null;
        }
    }

    private PathNode walkDirectional(PathfinderContext context, PathNode node, Direction direction) {
        ImmutableWorldVector walkingTo = node.add(direction).asImmutable();
        BoundingBox agentBounds = agent.characteristics().getBounds().shift(node.add(blockOffset, 0, blockOffset).asBukkit());

        switch (determineType(context, agentBounds, direction, walkingTo, node)) {
            case FALL:
                ImmutableWorldVector fallVec = fallTest(context.blockProvider(), walkingTo, agentBounds, direction);
                return fallVec == null ? null : node.chain(fallVec);
            case INCREASE:
                MutableWorldVector jumpVec = jumpTest(agentBounds, context.blockProvider(), walkingTo, direction);
                return jumpVec == null ? null : node.chain(jumpVec);
            case NO_CHANGE:
                return node.chain(walkingTo);
        }

        return null;
    }

    private JunctionType determineType(PathfinderContext context, BoundingBox agentBounds, Direction direction,
                                       ImmutableWorldVector target, PathNode current) {
        if(collidesMovingAlong(agentBounds, context.blockProvider(), direction, current.position().asImmutable())) {
            return JunctionType.INCREASE;
        }
        else {
            BoundingBox shiftedBounds = agentBounds.clone().shift(direction.asBukkit()).expandDirectional(Direction.DOWN.asBukkit());
            List<BlockSnapshot> collidingSnapshots = context.blockProvider().collidingSolids(shiftedBounds);

            if(collidingSnapshots.size() > 0) {
                //we hit something
                double highestY = -1;
                BlockSnapshot highestSnapshot = highestSnapshot(collidingSnapshots);

                if(highestSnapshot != null) {
                    double newY = highestSnapshot.position().blockY() + highestY;

                    if(newY == target.y()) {
                        return JunctionType.NO_CHANGE;
                    }

                    return JunctionType.FALL;
                }

                //this should never happen, but if it does, ignore the node
                return JunctionType.IGNORE;
            }

            return JunctionType.FALL;
        }
    }

    private MutableWorldVector jumpTest(BoundingBox agentBounds, BlockCollisionProvider provider,
                                        ImmutableWorldVector start, Direction direction) {
        double jumpHeightRequired = 0;
        double headroom = 0;
        double spillover = 0; //this helps us account for blocks with collision height larger than 1

        double jumpHeight = agent.characteristics().jumpHeight();
        double height = agent.characteristics().height();

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
                ImmutableWorldVector jumpedVector = start.add(0, jumpHeightRequired, 0);
                if(!collidesMovingAlong(jumpedAgent, provider, direction, jumpedVector)) {
                    return jumpedVector.asMutable();
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

    private ImmutableWorldVector fallTest(BlockCollisionProvider provider, ImmutableWorldVector vector,
                                          BoundingBox agentBounds, Direction direction) {
        BoundingBox bounds = agentBounds.clone().shift(direction.asBukkit());

        double deltaY = 0;
        while(bounds.getMinY() > 0) {
            bounds.expandDirectional(Direction.DOWN.asBukkit());

            List<BlockSnapshot> snapshots = provider.collidingSolids(bounds);
            if(snapshots.size() > 0) {
                BlockSnapshot highest = highestSnapshot(snapshots);

                if(highest != null) {
                    deltaY -= 1 - highest.collision().maxY();
                    return vector.add(0, deltaY, 0);
                }

                return null;
            }

            deltaY--;
        }

        return null;
    }

    private boolean collidesMovingAlong(BoundingBox agentBounds, BlockCollisionProvider provider, Direction direction,
                                        ImmutableWorldVector nodePosition) {
        BoundingBox expanded = agentBounds.clone().expandDirectional(direction.asBukkit());

        if(!direction.isIntercardinal()) {
            return provider.collidesWithAnySolid(expanded);
        }

        List<BlockSnapshot> candidates = provider.collidingSolids(expanded);
        if(candidates.size() > 0) {
            int dirFactor = direction.blockX() * direction.blockZ();
            return processCollisions(candidates, dirFactor < 0 ? collision -> fastDiagonalCollisionCheck(width,
                    negativeWidth, dirFactor, collision.getMinX(), collision.getMinZ(), collision.getMaxX(),
                    collision.getMaxZ()) : collision -> fastDiagonalCollisionCheck(width, negativeWidth,
                    dirFactor, collision.getMaxX(), collision.getMinZ(), collision.getMinX(), collision.getMaxZ()),
                    nodePosition.blockX() + 0.5, nodePosition.blockZ() + 0.5);
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
    private boolean fastDiagonalCollisionCheck(double width, double negativeWidth, int dirFac, double minX, double minZ,
                                               double maxX, double maxZ) {
        double zMinusXMin = minZ - (minX * dirFac);
        if(!(zMinusXMin <= width)) {
            return maxZ - (maxX * dirFac) <= width;
        }

        if(zMinusXMin >= negativeWidth) {
            return true;
        }

        return maxZ - (maxX * dirFac) >= negativeWidth;
    }

    private boolean processCollisions(List<BlockSnapshot> candidates, Function<BoundingBox, Boolean> collides,
                                      double agentX, double agentZ) {
        for(BlockSnapshot snapshot : candidates) {
            for(BoundingBox collision : snapshot.collision().boundingBoxes()) {
                collision.shift(snapshot.position().asBukkit()).shift(-agentX, 0, -agentZ);

                if(collides.apply(collision)) {
                    return true;
                }
            }
        }

        return false;
    }

    private @Nullable BlockSnapshot highestSnapshot(List<BlockSnapshot> collidingSnapshots) {
        double highestY = -1;
        BlockSnapshot highestSnapshot = null;
        for(BlockSnapshot snapshot : collidingSnapshots) {
            VoxelShapeWrapper voxelShape = snapshot.collision();

            if(voxelShape.maxY() > highestY) {
                highestY = voxelShape.maxY();
                highestSnapshot = snapshot;
            }
        }

        return highestSnapshot;
    }
}