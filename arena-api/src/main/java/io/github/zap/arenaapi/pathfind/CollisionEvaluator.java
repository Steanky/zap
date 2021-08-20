package io.github.zap.arenaapi.pathfind;

import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public interface CollisionEvaluator {
    boolean collidesMovingAlong(@NotNull BlockCollisionProvider provider, @NotNull Direction direction,
                                @NotNull BoundingBox agentBounds);
}
