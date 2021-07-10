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

    private PathNode walkDirectional(PathfinderContext context, PathNode currentNode, Direction direction) {
        ImmutableWorldVector currentPos = currentNode.position().asImmutable();
        ImmutableWorldVector walkingTo = currentNode.add(direction).asImmutable();

        VectorAccess translateOffset;
        BoundingBox agentBoundsAtCurrentNode;

        if(currentNode.isFirst) {
            //for the first node, collision check from the agent's start position
            agentBoundsAtCurrentNode = agent.characteristics().getBounds().shift(agentStartPosition.asBukkit());
            translateOffset = computeTranslation(currentPos, walkingTo.add(0.5, 0, 0.5), direction);
        }
        else {
            //for any other nodes, assume the agent is centered on the block
            agentBoundsAtCurrentNode = agent.characteristics().getBounds().shift(currentNode.add(blockHorizontalOffset,
                    0, blockHorizontalOffset).asBukkit());

            BlockSnapshot blockAtCurrentNode = context.blockProvider().getBlock(currentNode);
            if(blockAtCurrentNode != null) {
                double height = blockAtCurrentNode.collision().maxY();

                if(Double.isFinite(height) && !DoubleMath.fuzzyEquals(height, 1, Vector.getEpsilon())) {
                    double offset = 1 - height;
                    agentBoundsAtCurrentNode.shift(0, offset, 0);
                    currentPos = currentPos.add(0, offset, 0);
                }
            }
            else {
                //if we can't access the block at our location, we're probably in an unloaded area
                return null;
            }

            translateOffset = direction;
        }

        agentBoundsAtCurrentNode.shift(direction.asBukkit().multiply(Vector.getEpsilon()));

        switch (determineType(context, agentBoundsAtCurrentNode, walkingTo, currentPos, direction)) {
            case FALL:
                ImmutableWorldVector fallVec = fallTest(context.blockProvider(), agentBoundsAtCurrentNode, walkingTo,
                        translateOffset.asImmutable());
                return fallVec == null ? null : currentNode.chain(fallVec);
            case INCREASE:
                ImmutableWorldVector jumpVec = jumpTest(context.blockProvider(), agentBoundsAtCurrentNode, walkingTo,
                        currentPos, direction, translateOffset.asImmutable());
                return jumpVec == null ? null : currentNode.chain(jumpVec);
            case NO_CHANGE:
                return currentNode.chain(walkingTo);
        }

        return null;
    }

    private JunctionType determineType(PathfinderContext context, BoundingBox agentBounds, ImmutableWorldVector target,
                                       ImmutableWorldVector translateBy, Direction direction) {
        if(collidesMovingAlong(agentBounds, context.blockProvider(), direction, translateBy)) {
            //mobs are not really capable of jumping diagonally correctly, so don't try to pathfind like this
            return direction.isIntercardinal() ? JunctionType.IGNORE : JunctionType.INCREASE;
        }
        else {
            BoundingBox shiftedBounds = agentBounds.clone().shift(translateBy.asBukkit())
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

    private ImmutableWorldVector jumpTest(BlockCollisionProvider provider, BoundingBox agentBounds,
                                          ImmutableWorldVector entityPos, ImmutableWorldVector walkingTo,
                                          Direction direction, ImmutableWorldVector translateBy) {
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

                if(!collidesMovingAlong(jumpedAgent, provider, direction, translateBy)) {
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

    private ImmutableWorldVector fallTest(BlockCollisionProvider provider, BoundingBox agentBounds,
                                          ImmutableWorldVector startVector, ImmutableWorldVector translateBy) {
        BoundingBox boundsAtNew = agentBounds.clone().shift(translateBy.asBukkit());
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
                    return startVector.subtract(0, (startVector.y() - highest.position().y() - 1) - contribution, 0);
                }

                return null;
            }

            iters++;
        }

        return null;
    }

    private boolean collidesMovingAlong(BoundingBox agentBounds, BlockCollisionProvider provider, Direction direction,
                                        ImmutableWorldVector translateBy) {
        BoundingBox expandedBounds = agentBounds.clone().expandDirectional(translateBy.asBukkit());

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
                            dirFactor, x2, z, x, z2), agentBounds.getCenterX(), agentBounds.getCenterZ());
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

    private ImmutableWorldVector computeTranslation(ImmutableWorldVector currentPos, ImmutableWorldVector targetPos,
                                                    Direction direction) {
        double xOffset = Math.abs(targetPos.x() - currentPos.x());
        double zOffset = Math.abs(targetPos.z() - currentPos.z());

        return VectorAccess.immutable(xOffset * direction.x(), 0, zOffset * direction.z());
    }
}