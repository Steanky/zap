package io.github.zap.arenaapi.pathfind.step;

import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.pathfind.chunk.ChunkCoordinateProvider;
import io.github.zap.arenaapi.pathfind.agent.PathAgent;
import io.github.zap.arenaapi.pathfind.path.PathNode;
import io.github.zap.arenaapi.pathfind.path.PathNodeFactory;
import io.github.zap.arenaapi.pathfind.context.PathfinderContext;
import io.github.zap.arenaapi.pathfind.util.Direction;
import io.github.zap.vector.Vector3D;
import io.github.zap.vector.Vector3I;
import io.github.zap.vector.Vectors;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord") //unintelliJ
class WalkNodeExplorer implements NodeExplorer {
    private final NodeStepper stepper;
    private final ChunkCoordinateProvider chunkBounds;

    WalkNodeExplorer(@NotNull NodeStepper stepper, @NotNull ChunkCoordinateProvider chunkBounds) {
        this.stepper = stepper;
        this.chunkBounds = chunkBounds;
    }

    public <T extends PathNode> void exploreNodes(@NotNull PathfinderContext context, @NotNull PathAgent agent, T[] buffer,
                                                  @NotNull T current, @NotNull PathNodeFactory<T> pathNodeFactory) {
        if(buffer == null) {
            return;
        }

        BlockCollisionView blockAtCurrent = context.blockProvider().getBlock(current);

        if(blockAtCurrent == null) { //return if we have no block at the current node
            if(buffer.length > 0) {
                buffer[0] = null;
            }

            return;
        }

        Vector3D position;
        if(Vectors.equals(Vectors.asIntFloor(agent), current)) { //use exact agent position for first node...
            position = agent;
        }
        else { //...otherwise, make the assumption it's trying to pathfind from the exact center of the block
            position = Vectors.of(current.x() + 0.5, blockAtCurrent.exactY(), current.z() + 0.5);
        }

        int j = 0;
        for(int i = 0; i < buffer.length; i++) { //try to go all 8 cardinal/intercardinal directions as well as up
            Direction direction = Direction.valueAtIndex(i);
            Vector3I nextTarget = Vectors.add(current, direction);

            if(chunkBounds.hasBlock(nextTarget)) {
                Vector3I nodePosition = stepper.stepDirectional(context.blockProvider(), agent, position, direction);

                if(nodePosition != null && chunkBounds.hasBlock(nodePosition)) {
                    T newNode = pathNodeFactory.make(nodePosition);

                    if(blockAtCurrent.collision().isPartial() && nodePosition.y() > current.y()) {
                        newNode.setOffsetVector(Direction.UP);
                    }

                    buffer[j++] = newNode;
                }
            }
        }

        if(j < buffer.length) {
            buffer[j] = null;
        }
    }
}

