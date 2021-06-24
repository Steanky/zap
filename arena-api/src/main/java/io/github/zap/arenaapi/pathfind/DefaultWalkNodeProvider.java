package io.github.zap.arenaapi.pathfind;

import io.github.zap.nms.common.world.BlockCollisionSnapshot;
import io.github.zap.vector.MutableWorldVector;
import io.github.zap.vector.VectorAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultWalkNodeProvider extends NodeProvider {
    @Override
    public @NotNull PathNode[] generateNodes(@NotNull PathfinderContext context, @NotNull PathAgent agent,
                                             @NotNull PathNode at) {

        return new PathNode[0];
    }

    /**
     * Simplified, faster algorithm for entities whose width is < 1
     */
    private @Nullable PathNode tryWalkDirectionalSimplified(@NotNull PathfinderContext context, @NotNull PathAgent agent,
                                                  @NotNull PathNode node, @NotNull Direction direction) {
        PathNode candidate = node.chain(direction);
        MutableWorldVector nodePosition = candidate.position().asMutable();

        return null;
    }

    private double requiredJumpHeight(PathAgent agent, BlockCollisionProvider provider, MutableWorldVector nodePosition) {
        double jumpHeightRequired = 0;
        double headroom = 0;
        double excessHeight = 0;

        for(int i = 0; i < agent.characteristics().jumpHeight; i++) {
            BlockCollisionSnapshot blockCollisionSnapshot = provider.getBlock(nodePosition);
            if(blockCollisionSnapshot.data().getMaterial().isSolid()) {
                double currentHeight = blockCollisionSnapshot.height();

                currentHeight -= excessHeight;
                jumpHeightRequired += currentHeight;
                excessHeight = currentHeight - 1;

                if(currentHeight > agent.characteristics().jumpHeight) {
                    return -1;
                }
            }
            else {
                if(++headroom <= agent.characteristics().height) {
                    return jumpHeightRequired;
                }

                i--;
            }

            nodePosition.add(Direction.UP);
        }

        return -1;
    }
}
