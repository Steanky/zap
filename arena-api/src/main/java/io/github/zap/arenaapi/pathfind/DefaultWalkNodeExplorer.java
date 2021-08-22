package io.github.zap.arenaapi.pathfind;

import com.google.common.math.DoubleMath;
import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.vector.Vector3D;
import io.github.zap.vector.Vector3I;
import io.github.zap.vector.Vectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class DefaultWalkNodeExplorer implements NodeExplorer {
    private final PathAgent agent;
    private final NodeStepper stepper;

    DefaultWalkNodeExplorer(@NotNull PathAgent agent, @NotNull NodeStepper stepper) {
        this.agent = agent;
        this.stepper = stepper;
    }

    @Override
    public void exploreNodes(@NotNull PathfinderContext context, @Nullable PathNode[] buffer, @NotNull PathNode current) {
        BlockCollisionView blockAtCurrent = context.blockProvider().getBlock(current);

        if(blockAtCurrent == null) { //return if we have no block at the current node
            if(buffer.length > 0) {
                buffer[0] = null;
            }

            return;
        }

        Vector3D position;
        if(Vectors.equals(Vectors.asIntFloor(agent), current)) {
            position = agent;
        }
        else { //otherwise make the assumption it's trying to pathfind from the exact center of the block
            position = Vectors.of(current.x() + 0.5, blockAtCurrent.exactY(), current.z() + 0.5);
        }

        int j = 0;
        for(int i = 0; i < buffer.length; i++) { //try to go all 8 cardinal + intercardinal directions
            Direction direction = Direction.valueAtIndex(i);
            Vector3I nodePosition = stepper.stepDirectional(context.blockProvider(), position, direction);

            if(nodePosition != null) {
                PathNode node = new PathNode(nodePosition);
                BlockCollisionView block = context.blockProvider().getBlock(nodePosition);

                if(block != null && DoubleMath.fuzzyCompare(block.collision().maxY(), 0.5, Vectors.EPSILON) > 0) {
                    node.isPartialBlock = true;
                }

                buffer[j++] = node;
            }
        }

        if(j < buffer.length) {
            buffer[j] = null;
        }
    }

    @Override
    public boolean comparesWith(@NotNull NodeExplorer other) {
        return other instanceof DefaultWalkNodeExplorer;
    }
}

