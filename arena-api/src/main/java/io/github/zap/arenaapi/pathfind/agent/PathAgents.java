package io.github.zap.arenaapi.pathfind.agent;

import io.github.zap.vector.Vector3D;
import org.jetbrains.annotations.NotNull;

public final class PathAgents {
    public static @NotNull PathAgent fromVector(@NotNull Vector3D vector, double width, double height,
                                                double jumpHeight, double fallTolerance) {
        return new PathAgentImpl(vector.x(), vector.y(), vector.z(), width, height, jumpHeight, fallTolerance);
    }
}
