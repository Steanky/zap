package io.github.zap.arenaapi.pathfind;

import com.google.common.math.DoubleMath;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.nms.common.world.BlockSnapshot;
import io.github.zap.nms.common.world.BoxPredicate;
import io.github.zap.nms.common.world.VoxelShapeWrapper;
import io.github.zap.vector.ImmutableWorldVector;
import io.github.zap.vector.MutableWorldVector;
import io.github.zap.vector.VectorAccess;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DefaultWalkNodeExplorer extends NodeExplorer {
    private enum JunctionType {
        FALL,
        INCREASE,
        NO_CHANGE,
        IGNORE
    }

    private static final int MAX_FALL_TEST_ITERS = 8;

    private PathfinderContext context;
    private PathAgent agent;
    private ImmutableWorldVector agentStartPosition;

    private double blockHorizontalOffset;

    private double width;
    private double negativeWidth;

    public DefaultWalkNodeExplorer(@NotNull AversionCalculator aversionCalculator) {
        super(aversionCalculator);
    }

    @Override
    public void init(@NotNull PathfinderContext context, @NotNull PathAgent agent) {
        this.context = context;
        this.agent = agent;
        this.agentStartPosition = agent.position().asImmutable();

        width = agent.characteristics().width();
        negativeWidth = -width;

        blockHorizontalOffset = 0.5D - (width / 2D);
    }

    @Override
    public void exploreNodes(@Nullable PathNode[] buffer, @NotNull PathNode current) {
        int j = 0;
        for(int i = 0; i < buffer.length; i++) {
            PathNode node = walkDirectional(context, current, Direction.valueAtIndex(i));

            if(node != null) {
                calculateAversion(node, context.blockProvider());
                buffer[j++] = node;
            }
        }

        if(j < buffer.length) {
            buffer[j] = null;
        }
    }

    private PathNode walkDirectional(PathfinderContext context, PathNode node, Direction direction) {
        ImmutableWorldVector currentPos = node.position().asImmutable();
        ImmutableWorldVector walkingTo = node.add(direction).asImmutable();
        BoundingBox agentBoundsAtCurrentNode = agent.characteristics().getBounds().shift(node.add(blockHorizontalOffset,
                0, blockHorizontalOffset).asBukkit()).shift(direction.asBukkit().multiply(Vector.getEpsilon()));

        BlockSnapshot block = context.blockProvider().getBlock(node);
        if(block != null) {
            double height = block.collision().maxY();

            if(Double.isFinite(height) && !DoubleMath.fuzzyEquals(height, 1, Vector.getEpsilon())) {
                double offset = 1 - height;
                agentBoundsAtCurrentNode.shift(0, offset, 0);
                currentPos = currentPos.add(0, offset, 0);
            }
        }

        switch (determineType(context, agentBoundsAtCurrentNode, direction, walkingTo, currentPos)) {
            case FALL:
                ImmutableWorldVector fallVec = fallTest(context.blockProvider(), walkingTo, agentBoundsAtCurrentNode, direction);
                return fallVec == null ? null : node.chain(fallVec);
            case INCREASE:
                ImmutableWorldVector jumpVec = jumpTest(agentBoundsAtCurrentNode, context.blockProvider(), walkingTo,
                        currentPos, direction);
                return jumpVec == null ? null : node.chain(jumpVec);
            case NO_CHANGE:
                return node.chain(walkingTo);
        }

        return null;
    }

    private JunctionType determineType(PathfinderContext context, BoundingBox agentBounds, Direction direction,
                                       ImmutableWorldVector target, ImmutableWorldVector current) {
        if(collidesMovingAlong(agentBounds, context.blockProvider(), direction, current)) {
            //mobs are not really capable of jumping diagonally correctly
            return direction.isIntercardinal() ? JunctionType.IGNORE : JunctionType.INCREASE;
        }
        else {
            BoundingBox shiftedBounds = agentBounds.clone().shift(direction.asBukkit())
                    .expandDirectional(Direction.DOWN.asBukkit())
                    .resize(agentBounds.getMinX(), agentBounds.getMinY(), agentBounds.getMinZ(), agentBounds.getMaxX(),
                            agentBounds.getMinY() + 1 - Vector.getEpsilon(), agentBounds.getMaxZ());

            List<BlockSnapshot> collidingSnapshots = context.blockProvider().collidingSolids(shiftedBounds);

            if(!collidingSnapshots.isEmpty()) {
                //we hit something
                BlockSnapshot highestSnapshot = highestSnapshot(collidingSnapshots);

                if(highestSnapshot != null) {
                    double newY = highestSnapshot.position().blockY() + highestSnapshot.collision().maxY();

                    if(DoubleMath.fuzzyEquals(newY, target.y(), Vector.getEpsilon())) {
                        return JunctionType.NO_CHANGE;
                    }

                    return JunctionType.FALL;
                }

                //this should never happen, but if it somehow does, ignore the node
                ArenaApi.warning("Couldn't find highestSnapshot; ignoring node");
                return JunctionType.IGNORE;
            }

            return JunctionType.FALL;
        }
    }

    private ImmutableWorldVector jumpTest(BoundingBox agentBounds, BlockCollisionProvider provider,
                                          ImmutableWorldVector walkingTo, ImmutableWorldVector entityPos,
                                          Direction direction) {
        double jumpHeightRequired = entityPos.blockY() - entityPos.y();

        double headroom = 0;
        double spillover = 0; //this helps us account for blocks with collision height larger than 1

        double jumpHeight = agent.characteristics().jumpHeight();
        double height = agent.characteristics().height();

        MutableWorldVector seek = walkingTo.asMutable();

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
                    jumpHeightRequired -= newSpillover;
                    spillover = 0;
                }
                else {
                    spillover = newSpillover;
                }

                if(headroom != 0) {
                    jumpHeightRequired += headroom;
                    headroom = 0;
                }
            }
            else { //anything that does not fall into the above categories
                double newSpillover = spillover - maxY;

                if(newSpillover <= 0) { //maxY larger or equal to spillover
                    spillover = maxY - 1;
                    jumpHeightRequired -= newSpillover;

                    double headroomIncrement = minY - spillover;
                    if(headroomIncrement > 0) {
                        headroom += headroomIncrement;
                    }
                }
                else { //maxY smaller than spillover
                    spillover = newSpillover;

                    if(headroom != 0) {
                        jumpHeightRequired += headroom;
                        headroom = 0;
                    }
                }
            }

            if(jumpHeight >= jumpHeightRequired && height <= headroom) { //entity can make the jump
                BoundingBox verticalTest = agentBounds.clone().expandDirectional(0, jumpHeightRequired, 0);

                if(provider.collidesWithAny(verticalTest)) { //check if mob will collide with something on its way up
                    return null;
                }

                BoundingBox jumpedAgent = agentBounds.clone().shift(0, jumpHeightRequired, 0);
                ImmutableWorldVector jumpedVector = walkingTo.add(0, jumpHeightRequired, 0);

                if(!collidesMovingAlong(jumpedAgent, provider, direction, jumpedVector)) {
                    return jumpedVector.asImmutable();
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
        BoundingBox boundsAtNew = agentBounds.clone().shift(direction.asBukkit());
        boundsAtNew.resize(boundsAtNew.getMinX(), boundsAtNew.getMinY(), boundsAtNew.getMinZ(), boundsAtNew.getMaxX(),
                boundsAtNew.getMinY() + 1, boundsAtNew.getMaxZ());

        int iters = 0;
        while(boundsAtNew.getMinY() > 0 && iters < MAX_FALL_TEST_ITERS) {
            boundsAtNew.shift(0, -1, 0);

            List<BlockSnapshot> snapshots = provider.collidingSolids(boundsAtNew);
            if(!snapshots.isEmpty()) {
                BlockSnapshot highest = highestSnapshot(snapshots);

                if(highest != null) {
                    double contribution = 1 - highest.collision().maxY();
                    return vector.subtract(0, (vector.y() - highest.position().y() - 1) - contribution, 0);
                }

                return null;
            }

            iters++;
        }

        return null;
    }

    private boolean collidesMovingAlong(BoundingBox agentBounds, BlockCollisionProvider provider, Direction direction,
                                        ImmutableWorldVector currentNode) {
        BoundingBox expandedBounds = agentBounds.clone().expandDirectional(direction.asBukkit());

        if(!direction.isIntercardinal()) {
            return provider.collidesWithAny(expandedBounds);
        }

        List<BlockSnapshot> candidates = provider.collidingSolids(expandedBounds);
        if(!candidates.isEmpty()) {
            int dirFactor = direction.blockX() * direction.blockZ();

            return processCollisions(candidates, dirFactor < 0 ? (x, y, z, x2, y2, z2) ->
                            fastDiagonalCollisionCheck(width, negativeWidth, dirFactor, x, z, x2, z2) :
                    (x, y, z, x2, y2, z2) ->
                            fastDiagonalCollisionCheck(width, negativeWidth,
                            dirFactor, x2, z, x, z2), currentNode.blockX() + 0.5,
                    currentNode.blockZ() + 0.5);
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
        if(!(DoubleMath.fuzzyCompare(zMinusXMin, width, Vector.getEpsilon()) == -1)) {
            return DoubleMath.fuzzyCompare(maxZ - (maxX * dirFac), width, Vector.getEpsilon()) == -1;
        }

        if(DoubleMath.fuzzyCompare(zMinusXMin, negativeWidth, Vector.getEpsilon()) == 1) {
            return true;
        }

        return DoubleMath.fuzzyCompare(maxZ - (maxX * dirFac), negativeWidth, Vector.getEpsilon()) == 1;
    }

    private boolean processCollisions(List<BlockSnapshot> candidates, BoxPredicate collisionChecker,
                                      double agentCenterX, double agentCenterZ) {
        for(BlockSnapshot snapshot : candidates) {
            double x = snapshot.position().blockX() - agentCenterX;
            double y = snapshot.position().blockY();
            double z = snapshot.position().blockZ() - agentCenterZ;

            if(snapshot.collision().anyBoundsMatches((minX, minY, minZ, maxX, maxY, maxZ) -> {
                minX += x;
                minY += y;
                minZ += z;

                maxX += x;
                maxY += y;
                maxZ += z;

                return collisionChecker.test(minX, minY, minZ, maxX, maxY, maxZ);
            })) {
                return true;
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
