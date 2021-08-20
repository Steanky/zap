package io.github.zap.arenaapi.pathfind;

import com.google.common.math.DoubleMath;
import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.vector.Vector3D;
import io.github.zap.vector.Vector3I;
import io.github.zap.vector.Vectors;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WalkNodeStepper implements NodeStepper {
    private final BlockCollisionProvider collisionProvider;
    private final AgentCharacteristics characteristics;
    private final CollisionEvaluator collisionEvaluator;

    private final double halfWidth;

    private Vector3D lastAgentPosition;
    private BoundingBox cachedAgentBounds = null;

    public WalkNodeStepper(@NotNull BlockCollisionProvider collisionProvider,
                           @NotNull AgentCharacteristics characteristics, @NotNull CollisionEvaluator collisionEvaluator) {
        this.collisionProvider = collisionProvider;
        this.characteristics = characteristics;
        this.collisionEvaluator = collisionEvaluator;
        this.halfWidth = characteristics.width() / 2;
    }

    @Override
    public @Nullable Vector3I stepDirectional(@NotNull Vector3D agentPosition, @NotNull Direction direction) {
        BoundingBox agentBounds = getAgentBounds(agentPosition);

        //if collides, compute jump (we know we aren't falling)
        //else, compute fall
        if(collisionEvaluator.collidesMovingAlong(collisionProvider, direction, agentBounds.clone())) {
            return computeJumpVector(agentBounds, direction);
        }
        else {
            return null;
        }
    }

    private Vector3I computeJumpVector(BoundingBox agentBounds, Direction direction) {
        BoundingBox directionShiftedBounds = agentBounds.clone().shift(direction.x(), direction.y(), direction.z());

        double totalDeltaY = 0;
        do {
            List<BlockCollisionView> collisions = collisionProvider.collidingSolids(directionShiftedBounds);

            double deltaY;
            if(collisions.isEmpty()) { //the agent can stand here; now validate they can actually make the jump
                BoundingBox shiftedBounds = agentBounds.clone().shift(0, totalDeltaY, 0);

                if(!collisionEvaluator.collidesMovingAlong(collisionProvider, direction, shiftedBounds)) {
                    BoundingBox expandedBounds = agentBounds.clone()
                            .expandDirectional(0, characteristics.jumpHeight(), 0)
                            .expandDirectional(0, -characteristics.height(), 0);

                    if(!collisionProvider.collidesWithAny(expandedBounds)) {
                        return Vectors.asIntFloor(directionShiftedBounds.getCenterX(), directionShiftedBounds.getMinY(),
                                directionShiftedBounds.getCenterZ());
                    }
                }

                deltaY = agentBounds.getHeight();
            }
            else {
                BlockCollisionView highest = selectHighest(collisions);
                deltaY = (highest.y() + highest.collision().maxY()) - directionShiftedBounds.getMinY();
            }

            totalDeltaY += deltaY;
            directionShiftedBounds.shift(0, deltaY, 0);
        }
        while(DoubleMath.fuzzyCompare(totalDeltaY, characteristics.jumpHeight(), Vectors.EPSILON) <= 0);

        return null;
    }

    private Vector3I computeFallVector(Direction direction) {
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

    private BlockCollisionView selectHighest(List<BlockCollisionView> collisions) {
        double largestHeight = Double.MIN_VALUE;
        BlockCollisionView highestBlock = null;

        for(BlockCollisionView collisionView : collisions) {
            double collisionHeight = collisionView.y() + collisionView.collision().maxY();

            if(collisionHeight > largestHeight) {
                largestHeight = collisionHeight;
                highestBlock = collisionView;
            }
        }

        return highestBlock;
    }
}