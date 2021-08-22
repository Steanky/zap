package io.github.zap.arenaapi.pathfind;

import com.google.common.math.DoubleMath;
import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.vector.Vector3D;
import io.github.zap.vector.Vector3I;
import io.github.zap.vector.Vectors;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

class WalkNodeStepper implements NodeStepper {
    private static final Vector3D BLOCK_OFFSET = Vectors.of(0.5, 0, 0.5);

    private final AgentCharacteristics characteristics;

    private final double halfWidth;

    private Vector3D lastAgentPosition;
    private BoundingBox cachedAgentBounds = null;

    public WalkNodeStepper(@NotNull AgentCharacteristics characteristics) {
        this.characteristics = characteristics;
        this.halfWidth = characteristics.width() / 2;
    }

    @Override
    public @Nullable Vector3I stepDirectional(@NotNull BlockCollisionProvider collisionProvider,
                                              @NotNull Vector3D agentPosition, @NotNull Direction direction) {
        Vector3D horizontalTranslation = computeHorizontalTranslation(agentPosition, direction);
        BoundingBox agentBounds = getAgentBounds(agentPosition);
        BoundingBox agentBoundsShifted = agentBounds.clone().shift(horizontalTranslation.x(),
                horizontalTranslation.y(), horizontalTranslation.z());

        if(collisionProvider.collidesMovingAlong(agentBounds, direction, horizontalTranslation)) {
            if(direction.isIntercardinal()) { //mobs can't jump diagonally
                return null;
            }

            Vector3D result = seekDirectional(collisionProvider, agentBoundsShifted, true);

            if(result != null) {
                double deltaY = result.y() - agentBounds.getMinY();

                if(!collisionProvider.collidesMovingAlong(agentBounds, Direction.UP, Vectors.of(0, deltaY, 0)) &&
                        !collisionProvider.collidesMovingAlong(agentBounds.clone().shift(0, deltaY, 0),
                                direction, horizontalTranslation)) {
                    return Vectors.asIntFloor(result);
                }
            }
        }
        else {
            Vector3D result = seekDirectional(collisionProvider, agentBoundsShifted, false);

            if(result != null) {
                return Vectors.asIntFloor(result);
            }
        }

        return null;
    }

    private Vector3D seekDirectional(BlockCollisionProvider collisionProvider, BoundingBox bounds, boolean isJump) {
        double maximumDelta = isJump ? characteristics.jumpHeight() : characteristics.maxFall();
        double delta = 0;

        do {
            List<BlockCollisionView> collisions = collisionProvider.collidingSolidsAt(bounds);

            double stepDelta;
            if(collisions.isEmpty()) {
                if(isJump) { //termination condition for jumping
                    return Vectors.of(bounds.getCenterX(), bounds.getMinY(), bounds.getCenterZ());
                }
                else {
                    stepDelta = -characteristics.height();
                }
            }
            else {
                BlockCollisionView highest = selectHighest(collisions);

                if(isJump) {
                    stepDelta = highest.exactY() - bounds.getMinY();
                }
                else { //termination condition for falling
                    return Vectors.of(bounds.getCenterX(), highest.exactY(), bounds.getCenterZ());
                }
            }

            bounds.shift(0, stepDelta, 0);
            delta += Math.abs(stepDelta);
        }
        while(DoubleMath.fuzzyCompare(delta, maximumDelta, Vectors.EPSILON) <= 0);

        return null;
    }

    private BoundingBox getAgentBounds(Vector3D agentPosition) {
        if(!agentPosition.equals(lastAgentPosition)) {
            cachedAgentBounds = characteristics.getBounds().shift(agentPosition.x() - halfWidth, agentPosition.y(),
                    agentPosition.z() - halfWidth);
            lastAgentPosition = agentPosition;
        }

        return cachedAgentBounds;
    }

    private BlockCollisionView selectHighest(Collection<BlockCollisionView> collisions) {
        double largestY = Double.MIN_VALUE;
        BlockCollisionView highestBlock = null;

        for(BlockCollisionView collisionView : collisions) {
            double maxY = collisionView.exactY();
            if(maxY > largestY) {
                largestY = maxY;
                highestBlock = collisionView;
            }
        }

        return highestBlock;
    }

    private Vector3D computeHorizontalTranslation(Vector3D agentPosition, Direction direction) {
        Vector3I agentBlockPosition = Vectors.asIntFloor(agentPosition);
        Vector3I targetBlock = Vectors.add(agentBlockPosition, direction);
        Vector3D targetBlockCenter = Vectors.add(targetBlock, BLOCK_OFFSET);
        return Vectors.of(targetBlockCenter.x() - agentPosition.x(), 0, targetBlockCenter.z() - agentPosition.z());
    }
}