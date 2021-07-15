package io.github.zap.arenaapi.pathfind;

import com.google.common.math.DoubleMath;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.nms.common.world.BlockSnapshot;
import io.github.zap.nms.common.world.BoxPredicate;
import io.github.zap.nms.common.world.VoxelShapeWrapper;
import io.github.zap.vector.MutableVector3I;
import io.github.zap.vector.Vector3D;
import io.github.zap.vector.Vector3I;
import io.github.zap.vector.Vectors;
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

    private double width;
    private double halfWidth;

    private Vector halfWidthVector = new Vector();
    private Vector blockOffsetVector = new Vector();

    private double negativeWidth;
    private double blockOffset;

    public DefaultWalkNodeExplorer(@NotNull AversionCalculator aversionCalculator) {
        super(aversionCalculator);
    }

    @Override
    public void init(@NotNull PathfinderContext context, @NotNull PathAgent agent) {
        this.context = context;
        this.agent = agent;

        width = agent.characteristics().width();
        negativeWidth = -width;

        halfWidth = width / 2D;
        halfWidthVector.setX(halfWidth);
        halfWidthVector.setZ(halfWidth);

        blockOffset = 0.5D - (halfWidth);
        blockOffsetVector.setX(blockOffset);
        blockOffsetVector.setZ(blockOffset);
    }

    @Override
    public void exploreNodes(@Nullable PathNode[] buffer, @NotNull PathNode current) {
        BlockSnapshot blockAtCurrent = context.blockProvider().getBlock(current);
        if(blockAtCurrent == null) { //return if we have no block at the current node
            if(buffer.length > 0) {
                buffer[0] = null;
            }

            return;
        }

        BoundingBox currentAgentBounds;
        double blockMaxY = 0;
        if(Vectors.equals(Vectors.asIntFloor(agent), current)) { //use precise agent position for the node it's currently standing at
            currentAgentBounds = agent.characteristics().getBounds().shift(Vectors.asBukkit(agent).subtract(halfWidthVector));
        }
        else { //otherwise make the assumption it's trying to pathfind from the exact center of the block
            VoxelShapeWrapper collision = blockAtCurrent.collision();
            currentAgentBounds = agent.characteristics().getBounds().shift(Vectors.asBukkit(current).add(blockOffsetVector));

            if(collision.isFull()) {
                ArenaApi.warning("Attempting to pathfind from a non-initial block with full collision bounds.");
                ArenaApi.warning("This should not be happening. Send this message to Steank:");
                ArenaApi.warning("World: " + context.blockProvider().world().getName());
                ArenaApi.warning("Node: " + current);
                ArenaApi.warning("Agent bounds (after offset): " + currentAgentBounds);
                ArenaApi.warning("BlockData: " + blockAtCurrent.data());
                ArenaApi.warning("In addition to this information, also include which map this error occurred in.");
            }

            double height = collision.maxY(); //block relative height
            if(Double.isFinite(height) && DoubleMath.fuzzyCompare(height, 1, Vectors.EPSILON) < 0) {
                blockMaxY = height;
                currentAgentBounds.shift(0, height, 0);
            }
        }

        int j = 0;
        for(int i = 0; i < buffer.length; i++) { //try to go all 8 cardinal + intercardinal directions
            Direction direction = Direction.valueAtIndex(i);
            Vector3I blockWalkingTo = Vectors.add(current, direction);

            Vector3D translation = Vectors.of(blockWalkingTo.x() - currentAgentBounds.getCenterX()
                    + 0.5, 0, blockWalkingTo.z() - currentAgentBounds.getCenterZ() + 0.5);

            PathNode node = validNodeDirectional(context.blockProvider(), currentAgentBounds, translation, blockWalkingTo,
                    direction, blockMaxY);

            if(node != null) {
                calculateAversion(node, context.blockProvider());
                buffer[j++] = node;
            }
        }

        if(j < buffer.length) {
            buffer[j] = null;
        }
    }

    private PathNode validNodeDirectional(BlockCollisionProvider provider, BoundingBox currentAgentBounds,
                                          Vector3D translation, Vector3I blockWalkingTo,
                                          Direction direction, double currentBlockMaxY) {
        switch (determineType(provider, currentAgentBounds, translation, direction)) {
            case FALL:
                Vector3I fallVec = fallTest(provider, currentAgentBounds, blockWalkingTo, translation,
                        currentBlockMaxY);
                return fallVec == null ? null : new PathNode(fallVec.x(), fallVec.y(), fallVec.z());
            case INCREASE:
                Vector3I jumpVec = jumpTest(provider, currentAgentBounds, blockWalkingTo, direction,
                        translation, currentBlockMaxY);
                return jumpVec == null ? null : new PathNode(jumpVec.x(), jumpVec.y(), jumpVec.z());
            case NO_CHANGE:
                return new PathNode(blockWalkingTo.x(), blockWalkingTo.y(), blockWalkingTo.z());
        }

        return null;
    }

    private JunctionType determineType(BlockCollisionProvider provider, BoundingBox currentAgentBounds,
                                       Vector3D translation, Direction direction) {
        if(collidesMovingAlong(provider, currentAgentBounds, translation, direction)) {
            //mobs are not really capable of jumping diagonally correctly, so don't try to pathfind like this
            return direction.isIntercardinal() ? JunctionType.IGNORE : JunctionType.INCREASE;
        }
        else {
            BoundingBox shiftedBounds = currentAgentBounds.clone().shift(Vectors.asBukkit(translation))
                    .expandDirectional(Vectors.asBukkit(Direction.DOWN))
                    .resize(currentAgentBounds.getMinX(), currentAgentBounds.getMinY(), currentAgentBounds.getMinZ(),
                            currentAgentBounds.getMaxX(), currentAgentBounds.getMinY() + 1, currentAgentBounds.getMaxZ());

            List<BlockSnapshot> collidingSnapshots = provider.collidingSolids(shiftedBounds);

            if(!collidingSnapshots.isEmpty()) {
                //we hit something
                BlockSnapshot highestSnapshot = highestSnapshot(collidingSnapshots);

                if(highestSnapshot != null) {
                    double newY = highestSnapshot.y() + highestSnapshot.collision().maxY();

                    if(DoubleMath.fuzzyEquals(newY, currentAgentBounds.getMinY(), Vector.getEpsilon())) {
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

    private Vector3I jumpTest(BlockCollisionProvider provider, BoundingBox agentBounds,
                                          Vector3I walkingTo, Direction direction,
                                          Vector3D translateBy, double currentBlockMaxY) {
        double jumpHeightRequired = -currentBlockMaxY;

        double headroom = 0;
        double spillover = 0; //this helps us account for blocks with collision height larger than 1

        double jumpHeight = agent.characteristics().jumpHeight();
        double height = agent.characteristics().height();

        MutableVector3I seek = Vectors.mutableOf(walkingTo);

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

            if(DoubleMath.fuzzyCompare(jumpHeight, jumpHeightRequired, Vector.getEpsilon()) >= 0 &&
                    DoubleMath.fuzzyCompare(height, headroom, Vector.getEpsilon()) <= 0) { //entity can make the jump
                BoundingBox jumpedAgent = agentBounds.clone().shift(0, jumpHeightRequired, 0);
                Vector3D jumpedVector = Vectors.add(walkingTo, Vectors.of(0, jumpHeightRequired + currentBlockMaxY, 0));

                BoundingBox verticalTest = agentBounds.clone().expandDirectional(0, jumpHeightRequired, 0);

                if(provider.collidesWithAny(verticalTest)) { //check if mob will collide with something on its way up
                    return null;
                }

                if(!collidesMovingAlong(provider, jumpedAgent, translateBy, direction)) {
                    return Vectors.asIntFloor(jumpedVector);
                }
                else {
                    return null;
                }
            }

            seek.setY(seek.y() + 1);

            if(seek.y() >= 256) {
                return null;
            }
        }

        return null;
    }

    private Vector3I fallTest(BlockCollisionProvider provider, BoundingBox agentBounds, Vector3I walkingTo,
                              Vector3D translateBy, double blockMaxY) {
        BoundingBox boundsAtTranslate = agentBounds.clone().shift(Vectors.asBukkit(translateBy));
        boundsAtTranslate.resize(boundsAtTranslate.getMinX(), boundsAtTranslate.getMinY(), boundsAtTranslate.getMinZ(),
                boundsAtTranslate.getMaxX(), boundsAtTranslate.getMinY() + 1, boundsAtTranslate.getMaxZ());

        int iters = 0;
        while(boundsAtTranslate.getMinY() > 0 && iters < MAX_FALL_TEST_ITERS) {
            boundsAtTranslate.shift(0, -1, 0);

            List<BlockSnapshot> snapshots = provider.collidingSolids(boundsAtTranslate);
            if(!snapshots.isEmpty()) {
                BlockSnapshot highest = highestSnapshot(snapshots);

                if(highest != null) {
                    double maxY = highest.collision().maxY();

                    if(Double.isFinite(maxY)) {
                        double fallHeight = (walkingTo.y() + blockMaxY) - (highest.y() + maxY);
                        return Vectors.asIntFloor(Vectors.subtract(walkingTo, Vectors.of(0, fallHeight, 0)));
                    }
                    else {
                        ArenaApi.warning("Non-finite value for maxY in fallTest");
                    }
                }

                return null;
            }

            iters++;
        }

        return null;
    }

    private boolean collidesMovingAlong(BlockCollisionProvider provider, BoundingBox agentBounds,
                                        Vector3D translateBy, Direction direction) {
        BoundingBox expandedBounds = agentBounds.clone().expandDirectional(Vectors.asBukkit(translateBy))
                .shift(Vectors.asBukkit(Vectors.multiply(direction, Vectors.EPSILON)));

        if(!direction.isIntercardinal()) {
            return provider.collidesWithAny(expandedBounds);
        }

        List<BlockSnapshot> candidates = provider.collidingSolids(expandedBounds);
        if(!candidates.isEmpty()) {
            int dirFactor = direction.x() * direction.z();

            return processCollisions(candidates, dirFactor < 0 ?
                    (x, y, z, x2, y2, z2) ->
                            fastDiagonalCollisionCheck(width, negativeWidth, dirFactor, x, z, x2, z2) :
                    (x, y, z, x2, y2, z2) ->
                            fastDiagonalCollisionCheck(width, negativeWidth,
                            dirFactor, x2, z, x, z2), agentBounds.getCenterX(), agentBounds.getCenterZ());
        }

        return false;
    }

    private void calculateAversion(PathNode node, BlockCollisionProvider provider) {
        BlockSnapshot standingOn = provider.getBlock(node.x(), node.y() - 1, node.z());

        if(standingOn != null) {
            double materialAversion = getAversionCalculator().aversionForMaterial(standingOn.data().getMaterial());
            double factor = getAversionCalculator().aversionFactor(node);
            node.score.setG(node.parent.score.getG() + materialAversion + (factor * Vectors.distanceSquared(node, node.parent)));
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
            double x = snapshot.x() - agentCenterX;
            double y = snapshot.y();
            double z = snapshot.z() - agentCenterZ;

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
            double sampleY = voxelShape.maxY();


            if(Double.isFinite(sampleY) && sampleY > highestY) {
                highestY = sampleY;
                highestSnapshot = snapshot;
            }
        }

        return highestSnapshot;
    }
}