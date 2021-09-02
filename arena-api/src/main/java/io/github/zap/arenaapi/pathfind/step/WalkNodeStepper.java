package io.github.zap.arenaapi.pathfind.step;

import com.google.common.math.DoubleMath;
import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.pathfind.agent.PathAgent;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProvider;
import io.github.zap.vector.Direction;
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

    private Vector3D lastAgentPosition;
    private BoundingBox cachedAgentBounds = null;

    WalkNodeStepper() {}

    @Override
    public @Nullable Vector3I stepDirectional(@NotNull BlockCollisionProvider collisionProvider,
                                              @NotNull PathAgent agent, @NotNull Vector3D position,
                                              @NotNull Direction direction) {
        return switch (direction) {
            case UP, NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST ->
                    doStep(collisionProvider, agent, position, direction);
            default -> null;
        };
    }

    private Vector3I doStep(BlockCollisionProvider collisionProvider, PathAgent agent, Vector3D position, Direction direction) {
        Vector3D translation = computeTranslation(position, direction);
        BoundingBox agentBounds = getAgentBounds(agent, position);
        BoundingBox agentBoundsAtTargetNode = agentBounds.clone().shift(translation.x(), translation.y(), translation.z());

        BlockCollisionProvider.HitResult jumpTestResult = collisionProvider
                .collisionMovingAlong(agentBounds, direction, translation);
        if(jumpTestResult.collides()) { //test if we need to jump
            if(direction.isIntercardinal() || direction == Direction.UP) { //mobs can't jump diagonally (thanks mojang)
                return null;
            }

            Vector3D seekResult = seekDirectional(collisionProvider, agent, agentBoundsAtTargetNode.clone(), true);

            double adjustedDistance = Math.sqrt(jumpTestResult.nearestDistanceSquared());
            Vector3D shiftVector = Vectors.multiply(direction, adjustedDistance);

            //agentBoundsAtTargetNode now shifted to the very edge of the nearest colliding block (not overlapping it)
            agentBoundsAtTargetNode.shift(shiftVector.x(), shiftVector.y(), shiftVector.z());

            if(seekResult != null) {
                double deltaY = seekResult.y() - agentBounds.getMinY();

                if(!collisionProvider.collisionMovingAlong(agentBoundsAtTargetNode, Direction.UP, Vectors.of(0, deltaY, 0)).collides() &&
                        !collisionProvider.collisionMovingAlong(agentBoundsAtTargetNode.clone().shift(0, deltaY, 0),
                                direction, translation).collides()) {
                    return Vectors.asIntFloor(seekResult);
                }
            }
        }
        else {
            Vector3D result = seekDirectional(collisionProvider, agent, agentBoundsAtTargetNode, false);

            if(result != null) {
                return Vectors.asIntFloor(result);
            }
        }

        return null;
    }

    //WARNING mutates shiftedBounds, currently is never called multiple times on the same execution path
    private Vector3D seekDirectional(BlockCollisionProvider collisionProvider, PathAgent agent, BoundingBox shiftedBounds,
                                     boolean isJump) {
        double maximumDelta = isJump ? agent.jumpHeight() : agent.fallTolerance();
        double delta = 0;

        do {
            List<BlockCollisionView> collisions = collisionProvider.solidsOverlapping(shiftedBounds);

            double stepDelta;
            if(collisions.isEmpty()) {
                if(isJump) { //termination condition for jumping
                    return Vectors.of(shiftedBounds.getCenterX(), shiftedBounds.getMinY(), shiftedBounds.getCenterZ());
                }
                else {
                    stepDelta = -agent.height();
                }
            }
            else {
                BlockCollisionView highest = selectHighest(collisions);

                if(isJump) {
                    stepDelta = highest.exactY() - shiftedBounds.getMinY();
                }
                else { //termination condition for falling
                    return Vectors.of(shiftedBounds.getCenterX(), highest.exactY(), shiftedBounds.getCenterZ());
                }
            }

            shiftedBounds.shift(0, stepDelta, 0);
            delta += Math.abs(stepDelta); //stepDelta i'm stuck
        }
        while(DoubleMath.fuzzyCompare(delta, maximumDelta, Vectors.EPSILON) <= 0);

        return null;
    }

    private BoundingBox getAgentBounds(PathAgent agent, Vector3D newAgentPosition) {
        if(lastAgentPosition == null || !Vectors.equals(newAgentPosition, lastAgentPosition)) {
            double halfWidth = agent.width() / 2;
            double height = agent.height();
            cachedAgentBounds = new BoundingBox(
                    newAgentPosition.x() - halfWidth,
                    newAgentPosition.y(),
                    newAgentPosition.z() - halfWidth,
                    newAgentPosition.x() + halfWidth,
                    newAgentPosition.y() + height,
                    newAgentPosition.z() + halfWidth);

            lastAgentPosition = newAgentPosition;
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

    private Vector3D computeTranslation(Vector3D agentPosition, Direction direction) {
        Vector3I agentBlockPosition = Vectors.asIntFloor(agentPosition);
        Vector3I targetBlock = Vectors.add(agentBlockPosition, direction);
        Vector3D targetBlockCenter = Vectors.add(targetBlock, BLOCK_OFFSET);
        return Vectors.of(targetBlockCenter.x() - agentPosition.x(), direction.y(),
                targetBlockCenter.z() - agentPosition.z());
    }
}